package com.example.bddmod.client;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Reports the active shader-loader capability without imposing a compile-time dependency on a
 * loader. The audience capture itself is loader-neutral and reads the currently bound final FBO.
 */
public final class ShaderCompatibility {
    public enum RouteStatus {
        VANILLA,
        OCULUS_NO_SHADER_PACK,
        OCULUS_ACTIVE_SHADER_PACK,
        IRIS_NO_SHADER_PACK,
        IRIS_ACTIVE_SHADER_PACK,
        UNSUPPORTED,
        CAPTURE_FAILED
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-shader-compatibility");
    private static final List<String> API_CLASSES = List.of(
            "net.irisshaders.iris.api.v0.IrisApi",
            "net.coderbot.iris.api.v0.IrisApi");

    private static boolean checked;
    private static RouteStatus status = RouteStatus.UNSUPPORTED;
    private static volatile String captureFailure = "";

    private ShaderCompatibility() {
    }

    public static RouteStatus routeStatus() {
        ensureChecked();
        return captureFailure.isEmpty() ? status : RouteStatus.CAPTURE_FAILED;
    }

    /** Returns a compact status for the optional route diagnostic panel. */
    public static String describeAudienceRoute() {
        return routeStatus().name();
    }

    public static String describeCaptureFailure() {
        if (captureFailure.isEmpty()) {
            return "NONE";
        }
        return captureFailure.length() <= 64 ? captureFailure : captureFailure.substring(0, 61) + "...";
    }

    public static void reportCaptureFailure(String failure) {
        String reason = failure == null || failure.isBlank() ? "unknown capture failure" : failure;
        if (!reason.equals(captureFailure)) {
            captureFailure = reason;
            LOGGER.warn("Audience framebuffer capture failed; output fallback is active: {}", reason);
        }
    }

    public static void reportCaptureSuccess() {
        if (!captureFailure.isEmpty()) {
            LOGGER.info("Audience framebuffer capture recovered; output fallback cleared");
            captureFailure = "";
        }
    }

    private static void ensureChecked() {
        if (checked) {
            return;
        }

        try {
            ModList modList = ModList.get();
            boolean oculus = modList.isLoaded("oculus");
            boolean iris = modList.isLoaded("iris");
            if (!oculus && !iris) {
                status = RouteStatus.VANILLA;
            } else {
                boolean active = shaderPackActive();
                if (oculus) {
                    status = active ? RouteStatus.OCULUS_ACTIVE_SHADER_PACK
                            : RouteStatus.OCULUS_NO_SHADER_PACK;
                } else {
                    status = active ? RouteStatus.IRIS_ACTIVE_SHADER_PACK
                            : RouteStatus.IRIS_NO_SHADER_PACK;
                }
            }
            LOGGER.info("Audience shader route detected as {}", status);
        } catch (Throwable throwable) {
            status = RouteStatus.UNSUPPORTED;
            LOGGER.warn("Could not inspect shader-loader state; audience capture will rely on the current OpenGL framebuffer",
                    throwable);
        }
        checked = true;
    }

    private static boolean shaderPackActive() {
        for (String className : API_CLASSES) {
            try {
                Class<?> apiClass = Class.forName(className, false, ShaderCompatibility.class.getClassLoader());
                Method getInstance = apiClass.getMethod("getInstance");
                Object api = getInstance.invoke(null);
                Method isShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
                return Boolean.TRUE.equals(isShaderPackInUse.invoke(api));
            } catch (ClassNotFoundException ignored) {
                // The next known API namespace may still be available.
            } catch (ReflectiveOperationException | LinkageError exception) {
                LOGGER.debug("Shader-loader API {} was unavailable", className, exception);
            }
        }
        throw new IllegalStateException("no compatible Iris/Oculus shader API was found");
    }
}
