package com.example.bddmod.client;

import com.example.bddmod.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders the production audience route and its optional diagnostic panel.
 *
 * <p>The off-screen route remains active while OBS is connected. The configuration flag controls
 * only the player-visible diagnostics, not whether OBS receives the audience output.</p>
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
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        boolean diagnosticsEnabled = Config.RENDER_ROUTE_TEST_ENABLED.get();
        boolean audienceEnabled = OBSMonitor.isConnected()
                && Config.TERROR_MODE_ENABLED.get();
        if (audienceEnabled && !invocationLogged) {
            invocationLogged = true;
            LOGGER.info("Audience GUI renderer active: {}x{}, recording={}, diagnostics={}",
                    width, height, recording, diagnosticsEnabled);
        }

        if (audienceEnabled) {
            // Finish GUI batches before changing the active framebuffer.
            graphics.flush();
            boolean rendered = AudienceRenderTargetManager.renderFrame(minecraft, audienceTarget -> {
                // Audience-only post-processing is applied when the copied texture is presented.
            });
            if (rendered) {
                AudienceWindowManager.present(minecraft, AudienceRenderTargetManager.getTarget(), recording);
            } else {
                AudienceWindowManager.release();
            }
        } else {
            AudienceWindowManager.release();
            AudienceRenderTargetManager.release();
        }

        if (diagnosticsEnabled) {
            // The diagnostic must remain visible even when the audience route itself fails.
            drawDiagnosticPanel(graphics, minecraft, width, height, recording);
            graphics.flush();
        }
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
                Component.literal("AUDIENCE ROUTE: " + ShaderCompatibility.describeAudienceRoute()),
                left + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("AUDIENCE WINDOW: " + AudienceWindowManager.describeState()),
                split + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("CAPTURE: " + AudienceRenderTargetManager.describeCapture()),
                left + 8, top + 74, 0xFFFFFFFF, true);
        graphics.drawString(minecraft.font,
                Component.literal("FBO: " + AudienceRenderTargetManager.describeState()),
                split + 8, top + 74, 0xFFFFFFFF, true);
    }
}
