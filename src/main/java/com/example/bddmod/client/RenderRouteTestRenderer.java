package com.example.bddmod.client;

import com.example.bddmod.Config;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small, deliberately conservative render-target probe used by the split-route test build.
 *
 * <p>This class verifies that an independent FBO can be allocated, resized, bound, cleared,
 * written to, and safely released from the main render path. It does not claim to be the
 * native OBS swap-buffer hook: OBS Game Capture still needs to be tested separately.</p>
 */
public final class RenderRouteTestRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-render-route");
    private static final int PLAYER_COLOR = 0xFF238B57;
    private static final int OBS_COLOR = 0xFFB83232;
    private static final int PANEL_TEXT = 0xFFFFFFFF;

    private static RenderTarget probeTarget;
    private static int targetWidth;
    private static int targetHeight;
    private static boolean failed;
    private static boolean invocationLogged;

    private RenderRouteTestRenderer() {
    }

    public static void render(GuiGraphics graphics, Minecraft minecraft, boolean recording) {
        if (!Config.RENDER_ROUTE_TEST_ENABLED.get()) {
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

        try {
            if (recording && !failed) {
                int targetWidth = minecraft.getWindow().getWidth();
                int targetHeight = minecraft.getWindow().getHeight();
                boolean targetChanged = ensureTarget(targetWidth, targetHeight);

                // Finish GUI batches before changing the active framebuffer.
                graphics.flush();
                probeTarget.bindWrite(true);
                probeTarget.clear(true);
                probeTarget.unbindWrite();
                minecraft.getMainRenderTarget().bindWrite(true);
                if (targetChanged && isReady()) {
                    LOGGER.debug("Route-test FBO ready at {}x{}", targetWidth, targetHeight);
                }
            }
        } catch (Throwable throwable) {
            failed = true;
            restoreMainTarget(minecraft);
            LOGGER.warn("Render-route FBO probe disabled after a rendering failure", throwable);
        }

        // The diagnostic must remain visible even when the FBO probe itself fails.
        drawDiagnosticPanel(graphics, minecraft, width, height, recording);
        // Submit the diagnostic immediately while this overlay owns the GUI pass.
        graphics.flush();
    }

    public static boolean isReady() {
        return probeTarget != null && !failed;
    }

    private static boolean ensureTarget(int width, int height) {
        if (probeTarget == null) {
            probeTarget = new TextureTarget(width, height, true, false);
            targetWidth = width;
            targetHeight = height;
            return true;
        }

        if (targetWidth != width || targetHeight != height) {
            probeTarget.resize(width, height, false);
            targetWidth = width;
            targetHeight = height;
            return true;
        }

        return false;
    }

    private static void restoreMainTarget(Minecraft minecraft) {
        try {
            minecraft.getMainRenderTarget().bindWrite(true);
        } catch (Throwable ignored) {
            // Rendering failure handling must never cascade into a second client failure.
        }
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
                Component.literal(isReady() ? "FBO: READY" : "FBO: NOT INITIALIZED"),
                left + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("HOOK: NOT INSTALLED"), split + 8, top + 54, PANEL_TEXT, true);
        graphics.drawString(minecraft.font,
                Component.literal("ROUTE TEST - NATIVE OBS SPLIT NOT ACTIVE"),
                left + 8, top + 74, 0xFFFFFFFF, true);
    }
}
