package com.example.bddmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/** Tracks the local player's projected head for the audience-only post-process. */
public final class AudienceHeadEffectController {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-audience-head");
    public static final int MOSAIC = 0;
    public static final int DISTORTION = 1;
    public static final int DEFORMATION = 2;
    private static final int EFFECT_COUNT = 3;
    private static final Random RANDOM = new Random();
    private static final boolean[] MODE_LOGGED = new boolean[EFFECT_COUNT];

    private static boolean active;
    private static int mode;
    private static float centerX;
    private static float centerY;
    private static float radiusX;
    private static float radiusY;

    private AudienceHeadEffectController() {
    }

    /** Clears a stale mask before the next group of rendered frames. */
    public static void beginFrame() {
        active = false;
    }

    public static void captureLocalPlayerHead(Player player, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player != minecraft.player || minecraft.level == null
                || !OBSMonitor.isConnected() || !com.example.bddmod.Config.TERROR_MODE_ENABLED.get()
                || minecraft.options.getCameraType() == CameraType.FIRST_PERSON) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.getPosition();
        Vec3 headCenter = player.getEyePosition(partialTick).subtract(0.0D, 0.12D, 0.0D);
        Vector3f left = new Vector3f(camera.getLeftVector()).mul(0.28F);
        Vector3f up = new Vector3f(camera.getUpVector()).mul(0.30F);
        Projection center = project(headCenter, cameraPosition);
        Projection horizontal = project(headCenter.add(left.x(), left.y(), left.z()), cameraPosition);
        Projection vertical = project(headCenter.add(up.x(), up.y(), up.z()), cameraPosition);
        if (center == null || horizontal == null || vertical == null) {
            return;
        }

        float projectedRadiusX = Math.abs(horizontal.x - center.x);
        float projectedRadiusY = Math.abs(vertical.y - center.y);
        if (projectedRadiusX < 0.006F || projectedRadiusY < 0.006F
                || projectedRadiusX > 0.45F || projectedRadiusY > 0.45F) {
            return;
        }

        active = true;
        mode = RANDOM.nextInt(EFFECT_COUNT);
        centerX = center.x;
        centerY = center.y;
        radiusX = projectedRadiusX * 1.35F;
        radiusY = projectedRadiusY * 1.35F;
        if (!MODE_LOGGED[mode]) {
            MODE_LOGGED[mode] = true;
            LOGGER.info("Audience player-head effect active: mode={}, center=({}, {}), radius=({}, {})",
                    modeName(mode), centerX, centerY, radiusX, radiusY);
        }
    }

    public static boolean isActive(boolean recording) {
        return active && recording;
    }

    public static int mode() {
        return mode;
    }

    public static float centerX() {
        return centerX;
    }

    public static float centerY() {
        return centerY;
    }

    public static float radiusX() {
        return radiusX;
    }

    public static float radiusY() {
        return radiusY;
    }

    private static Projection project(Vec3 worldPosition, Vec3 cameraPosition) {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vector3f relative = new Vector3f(
                (float) (worldPosition.x - cameraPosition.x),
                (float) (worldPosition.y - cameraPosition.y),
                (float) (worldPosition.z - cameraPosition.z));
        float depth = relative.dot(camera.getLookVector());
        if (depth <= 0.01F) {
            return null;
        }
        Vector4f clip = new Vector4f(
                -relative.dot(camera.getLeftVector()),
                relative.dot(camera.getUpVector()),
                -depth,
                1.0F);
        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        projection.transform(clip);
        if (clip.w <= 0.001F || !Float.isFinite(clip.w)) {
            return null;
        }
        float ndcX = clip.x / clip.w;
        float ndcY = clip.y / clip.w;
        if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)
                || ndcX < -1.5F || ndcX > 1.5F || ndcY < -1.5F || ndcY > 1.5F) {
            return null;
        }
        return new Projection((ndcX + 1.0F) * 0.5F, (ndcY + 1.0F) * 0.5F);
    }

    private static String modeName(int effectMode) {
        return switch (effectMode) {
            case MOSAIC -> "MOSAIC";
            case DISTORTION -> "DISTORTION";
            case DEFORMATION -> "DEFORMATION";
            default -> "UNKNOWN";
        };
    }

    private record Projection(float x, float y) {
    }
}
