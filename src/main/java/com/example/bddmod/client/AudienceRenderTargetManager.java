package com.example.bddmod.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Owns the off-screen framebuffer that feeds the independently capturable audience window.
 */
public final class AudienceRenderTargetManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-audience-target");

    private static RenderTarget target;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean failed;
    private static String lastFailure = "";
    private static String lastCaptureSource = "NOT CAPTURED";
    private static String lastLoggedCapture = "";
    private static final AudienceFrameCapture FRAME_CAPTURE = new AudienceFrameCapture.CurrentFramebuffer();

    private AudienceRenderTargetManager() {
    }

    /**
     * Runs one audience render pass and always restores Minecraft's main framebuffer afterwards.
     */
    public static boolean renderFrame(Minecraft minecraft, Consumer<RenderTarget> renderer) {
        RenderSystem.assertOnRenderThread();
        if (failed) {
            return false;
        }

        RenderTarget mainTarget = minecraft.getMainRenderTarget();
        int width = mainTarget.width;
        int height = mainTarget.height;
        if (width <= 0 || height <= 0) {
            return false;
        }

        try {
            boolean changed = ensureTarget(width, height);
            AudienceFrameCapture.CaptureResult capture = FRAME_CAPTURE.capture(target);
            lastCaptureSource = capture.source();
            if (!capture.success()) {
                ShaderCompatibility.reportCaptureFailure(capture.failure());
                throw new IllegalStateException(capture.failure());
            }
            logSuccessfulCapture(capture.source());
            target.bindWrite(true);
            renderer.accept(target);
            if (changed) {
                LOGGER.info("Audience render target ready at {}x{}", targetWidth, targetHeight);
            }
            return true;
        } catch (Throwable throwable) {
            disableAfterFailure(throwable);
            return false;
        } finally {
            restoreMainTarget(mainTarget);
        }
    }

    public static boolean isReady() {
        return target != null && !failed;
    }

    @Nullable
    public static RenderTarget getTarget() {
        return isReady() ? target : null;
    }

    public static String describeState() {
        if (failed) {
            return "FAILED: " + lastFailure;
        }
        if (target == null) {
            return "NOT INITIALIZED";
        }
        return "READY " + targetWidth + "x" + targetHeight;
    }

    public static String describeCapture() {
        return lastCaptureSource;
    }

    public static void release() {
        if (target == null && !failed) {
            return;
        }
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(AudienceRenderTargetManager::releaseOnRenderThread);
            return;
        }
        releaseOnRenderThread();
    }

    private static boolean ensureTarget(int width, int height) {
        if (target == null) {
            target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            targetWidth = width;
            targetHeight = height;
            return true;
        }

        if (targetWidth != width || targetHeight != height) {
            target.resize(width, height, Minecraft.ON_OSX);
            targetWidth = width;
            targetHeight = height;
            return true;
        }
        return false;
    }

    private static void disableAfterFailure(Throwable throwable) {
        failed = true;
        destroyTarget();
        lastFailure = throwable.getClass().getSimpleName() + ": " + String.valueOf(throwable.getMessage());
        LOGGER.warn("Audience render target disabled after a rendering failure", throwable);
    }

    private static void restoreMainTarget(RenderTarget mainTarget) {
        try {
            mainTarget.bindWrite(true);
        } catch (Throwable throwable) {
            LOGGER.error("Failed to restore Minecraft's main render target", throwable);
        }
    }

    private static void releaseOnRenderThread() {
        RenderSystem.assertOnRenderThread();
        boolean hadState = target != null || failed;
        destroyTarget();
        failed = false;
        lastFailure = "";
        lastCaptureSource = "NOT CAPTURED";
        lastLoggedCapture = "";
        if (hadState) {
            LOGGER.info("Audience render target released");
        }
    }

    private static void logSuccessfulCapture(String source) {
        ShaderCompatibility.RouteStatus routeStatus = ShaderCompatibility.routeStatus();
        String captureIdentity = routeStatus + " " + source;
        if (!captureIdentity.equals(lastLoggedCapture)) {
            lastLoggedCapture = captureIdentity;
            LOGGER.info("Audience final framebuffer capture succeeded for {} from {}", routeStatus, source);
        }
    }

    private static void destroyTarget() {
        RenderTarget oldTarget = target;
        target = null;
        targetWidth = 0;
        targetHeight = 0;
        if (oldTarget == null) {
            return;
        }
        try {
            oldTarget.destroyBuffers();
        } catch (Throwable throwable) {
            LOGGER.debug("Audience render target cleanup failed", throwable);
        }
    }
}
