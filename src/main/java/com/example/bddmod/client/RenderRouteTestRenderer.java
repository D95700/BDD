package com.example.bddmod.client;

import com.example.bddmod.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small, deliberately conservative render-target probe used by the split-route test build.
 *
 * <p>This class visualizes the audience render-target lifecycle. It does not claim to be the
 * final OBS output route: a separate capture surface still needs to be implemented.</p>
 */
public final class RenderRouteTestRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-render-route");
    private static final int PLAYER_COLOR = 0xFF238B57;
    private static final int OBS_COLOR = 0xFFB83232;
    private static final int PANEL_TEXT = 0xFFFFFFFF;

    private static boolean invocationLogged;

    private RenderRouteTestRenderer() {
    }

    public static void render(GuiGraphics graphics, Minecraft minecraft, boolean recording) {
        if (!Config.RENDER_ROUTE_TEST_ENABLED.get()) {
            AudienceRenderTargetManager.release();
            return;
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        if (!invocationLogged) {
            invocationLogged = true;
            LOGGER.info("Route-test GUI renderer active: {}x{}, recording={}, testEnabled={}",
                    width, height, recording, Config.RENDER_ROUTE_TEST_ENABLED.get());
        }

        if (recording) {
            // Finish GUI batches before changing the active framebuffer.
            graphics.flush();
            AudienceRenderTargetManager.renderFrame(minecraft, audienceTarget -> {
                // The first milestone owns lifecycle only; later steps render an audience frame here.
            });
        } else {
            AudienceRenderTargetManager.release();
        }

        // The diagnostic must remain visible even when the FBO probe itself fails.
        drawDiagnosticPanel(graphics, minecraft, width, height, recording);
        // Submit the diagnostic immediately while this overlay owns the GUI pass.
        graphics.flush();
    }

    public static boolean isReady() {
        return AudienceRenderTargetManager.isReady();
    }

    private static void drawDiagnosticPanel(GuiGraphics graphics, Minecraft minecraft, int width, int height,
                                            boolean recording) {
        int panelWidth = Math.min(560, width - 16);
        int panelHeight = 104;
        int left = 8;
        int top = 8;
        int split = left + panelWidth / 2;

        graphics.fill(left, top, left + panelWidth, top + panelHeight, 0xF0101010);
        graphics.fill(left, top, split, top + 26, PLAYER_COLOR);
        graphics.fill(split, top, left + panelWidth, top + 26, OBS_COLOR);
        graphics.drawString(minecraft.font, Component.literal("PLAYER VIEW"), left + 8, top + 8, PANEL_TEXT, true);
        graphics.drawString(minecraft.font, Component.literal("OBS TEST BUFFER"), split + 8, top + 8, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("OBS STATE: " + (recording ? "RECORDING" : "STANDBY")),
                left + 8, top + 36, recording ? 0xFFFF5555 : 0xFFFFFF55, true);
        graphics.drawString(minecraft.font,
                Component.literal("WEBSOCKET: " + OBSMonitor.getConnectionStatus()),
                split + 8, top + 36, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("AUDIENCE FBO: " + AudienceRenderTargetManager.describeState()),
                left + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("HOOK: NOT INSTALLED"), split + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("ROUTE TEST - NATIVE OBS SPLIT NOT ACTIVE"),
                left + 8, top + 74, 0xFFFFFFFF, true);
    }
}
