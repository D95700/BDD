package com.example.bddmod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Tracks short-lived audience-only reactions to local player damage. */
public final class AudienceTriggerController {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-audience-trigger");
    private static final float DECAY_PER_TICK = 0.075F;

    private static float damagePulse;
    private static int previousHurtTime;

    private AudienceTriggerController() {
    }

    public static void update(Minecraft minecraft, boolean recording) {
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || !recording) {
            previousHurtTime = 0;
            damagePulse = 0.0F;
            return;
        }

        int hurtTime = player.hurtTime;
        if (hurtTime > previousHurtTime) {
            damagePulse = 1.0F;
            LOGGER.info("Audience damage trigger activated");
        } else {
            damagePulse = Math.max(0.0F, damagePulse - DECAY_PER_TICK);
        }
        previousHurtTime = hurtTime;
    }

    public static float level(boolean recording) {
        return recording ? damagePulse : 0.0F;
    }
}
