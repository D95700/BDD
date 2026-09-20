package com.example.bddmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders a deterministic, edge-bound vein pattern while OBS recording is active.
 */
public final class RecordingVeinOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-recording-veins");
    private static final int DESIGN_SIZE = 1000;
    private static final int GLOW_RGB = 0x280307;
    private static final int CORE_RGB = 0x74121C;

    private static final int[][][] VEIN_PATHS = {
            {{0, 58}, {34, 57}, {62, 82}, {91, 76}, {119, 108}, {151, 116}},
            {{62, 82}, {51, 121}, {69, 158}},
            {{91, 76}, {109, 45}, {139, 30}, {157, 0}},
            {{0, 375}, {27, 367}, {52, 390}, {78, 397}, {101, 426}},
            {{52, 390}, {44, 424}, {59, 454}},
            {{1000, 58}, {966, 57}, {938, 82}, {909, 76}, {881, 108}, {849, 116}},
            {{938, 82}, {949, 121}, {931, 158}},
            {{909, 76}, {891, 45}, {861, 30}, {843, 0}},
            {{1000, 375}, {973, 367}, {948, 390}, {922, 397}, {899, 426}},
            {{948, 390}, {956, 424}, {941, 454}},
            {{0, 942}, {34, 943}, {62, 918}, {91, 924}, {119, 892}, {151, 884}},
            {{62, 918}, {51, 879}, {69, 842}},
            {{91, 924}, {109, 955}, {139, 970}, {157, 1000}},
            {{1000, 942}, {966, 943}, {938, 918}, {909, 924}, {881, 892}, {849, 884}},
            {{938, 918}, {949, 879}, {931, 842}},
            {{909, 924}, {891, 955}, {861, 970}, {843, 1000}},
            {{431, 0}, {438, 31}, {460, 49}, {467, 79}},
            {{569, 0}, {562, 31}, {540, 49}, {533, 79}},
            {{431, 1000}, {438, 969}, {460, 951}, {467, 921}},
            {{569, 1000}, {562, 969}, {540, 951}, {533, 921}}
    };

    private static boolean activationLogged;

    private RecordingVeinOverlay() {
    }

    public static void render(GuiGraphics graphics, int width, int height, double pulseLevel) {
        if (width <= 0 || height <= 0) {
            return;
        }

        if (!activationLogged) {
            activationLogged = true;
            LOGGER.info("Recording vein overlay active: {}x{}, paths={}", width, height, VEIN_PATHS.length);
        }

        double pulse = Math.max(0.0D, Math.min(1.0D, pulseLevel));

        for (int index = 0; index < VEIN_PATHS.length; index++) {
            int variation = (index % 3) * 3;
            drawPath(graphics, width, height, VEIN_PATHS[index], 1,
                    argb(8 + (int) (22 * pulse) + variation, GLOW_RGB));
            drawPath(graphics, width, height, VEIN_PATHS[index], 0,
                    argb(48 + (int) (82 * pulse) + variation, CORE_RGB));
        }

        graphics.flush();
    }

    private static void drawPath(GuiGraphics graphics, int width, int height, int[][] path, int radius, int color) {
        for (int index = 1; index < path.length; index++) {
            int startX = scale(path[index - 1][0], width);
            int startY = scale(path[index - 1][1], height);
            int endX = scale(path[index][0], width);
            int endY = scale(path[index][1], height);
            drawLine(graphics, width, height, startX, startY, endX, endY, radius, color);
        }
    }

    private static void drawLine(GuiGraphics graphics, int width, int height,
                                 int startX, int startY, int endX, int endY, int radius, int color) {
        int deltaX = Math.abs(endX - startX);
        int deltaY = Math.abs(endY - startY);
        int stepX = startX < endX ? 1 : -1;
        int stepY = startY < endY ? 1 : -1;
        int error = deltaX - deltaY;
        int x = startX;
        int y = startY;

        while (true) {
            fillPoint(graphics, width, height, x, y, radius, color);
            if (x == endX && y == endY) {
                return;
            }

            int doubledError = error * 2;
            if (doubledError > -deltaY) {
                error -= deltaY;
                x += stepX;
            }
            if (doubledError < deltaX) {
                error += deltaX;
                y += stepY;
            }
        }
    }

    private static void fillPoint(GuiGraphics graphics, int width, int height,
                                  int x, int y, int radius, int color) {
        int left = Math.max(0, x - radius);
        int top = Math.max(0, y - radius);
        int right = Math.min(width, x + radius + 1);
        int bottom = Math.min(height, y + radius + 1);
        if (left < right && top < bottom) {
            graphics.fill(RenderType.guiOverlay(), left, top, right, bottom, color);
        }
    }

    private static int scale(int coordinate, int dimension) {
        return Math.round(coordinate * (dimension - 1) / (float) DESIGN_SIZE);
    }

    private static int argb(int alpha, int rgb) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | rgb;
    }
}
