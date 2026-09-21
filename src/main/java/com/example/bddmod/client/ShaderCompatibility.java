package com.example.bddmod.client;

import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects shader-pack loaders that can replace Minecraft's framebuffer pipeline.
 *
 * <p>The audience output owns a second OpenGL context and blits Minecraft's main framebuffer.
 * Iris and Oculus may replace that pipeline, so the safest fallback is to leave the player's
 * normal renderer untouched and disable only the audience route.</p>
 */
public final class ShaderCompatibility {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-shader-compatibility");
    private static final List<String> SHADER_LOADER_IDS = List.of("iris", "oculus");

    private static boolean checked;
    private static List<String> detectedLoaders = List.of();

    private ShaderCompatibility() {
    }

    /**
     * Returns whether the independently capturable audience FBO is safe to use.
     * Detection is cached so the render loop does not repeatedly query the mod list or log.
     */
    public static boolean isAudienceRouteSupported() {
        ensureChecked();
        return detectedLoaders.isEmpty();
    }

    /** Returns a short diagnostic suitable for the optional in-game route panel. */
    public static String describeAudienceRoute() {
        ensureChecked();
        if (detectedLoaders.isEmpty()) {
            return "SUPPORTED";
        }
        return "FALLBACK (" + String.join(", ", detectedLoaders) + ")";
    }

    private static void ensureChecked() {
        if (checked) {
            return;
        }

        List<String> loaded = new ArrayList<>();
        try {
            ModList modList = ModList.get();
            for (String modId : SHADER_LOADER_IDS) {
                if (modList.isLoaded(modId)) {
                    loaded.add(modId);
                }
            }
        } catch (Throwable throwable) {
            loaded.add("mod-list-unavailable");
            LOGGER.warn("Could not inspect the Forge mod list; disabling the audience FBO route as a safety fallback",
                    throwable);
        }
        detectedLoaders = List.copyOf(loaded);
        checked = true;

        if (detectedLoaders.isEmpty()) {
            LOGGER.info("No Iris/Oculus shader loader detected; audience FBO route remains enabled");
        } else {
            LOGGER.warn("Detected shader loader(s) {}; disabling audience FBO route and keeping the player renderer unchanged",
                    String.join(", ", detectedLoaders));
        }
    }
}
