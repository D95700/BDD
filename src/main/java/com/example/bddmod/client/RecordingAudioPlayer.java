package com.example.bddmod.client;

import com.example.bddmod.Config;
import com.example.bddmod.sound.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecordingAudioPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-recording-audio");
    private static boolean active;
    private static double previousPhase = Double.NaN;

    private RecordingAudioPlayer() {
    }

    public static void start() {
        if (active) {
            return;
        }

        active = true;
        previousPhase = RecordingPulseController.phase();
        playBreath();
        LOGGER.info("Synchronized in-game breathing and heartbeat audio started");
    }

    public static void tick() {
        if (!RecordingPulseController.isActive()) {
            stop();
            return;
        }

        double currentPhase = RecordingPulseController.phase();
        if (!Double.isNaN(previousPhase)) {
            if (previousPhase > currentPhase) {
                playBreath();
            }
            for (int index = 0; index < RecordingPulseController.heartbeatCount(); index++) {
                double heartbeatPhase = RecordingPulseController.heartbeatPhase(index);
                if (crossedPhase(previousPhase, currentPhase, heartbeatPhase)) {
                    playHeartbeat(RecordingPulseController.heartbeatStrength(index));
                }
            }
        }
        previousPhase = currentPhase;
    }

    public static void stop() {
        boolean wasActive = active;
        active = false;
        previousPhase = Double.NaN;
        Minecraft.getInstance().getSoundManager().stop(
                ModSoundEvents.RECORDING_BREATH.get().getLocation(), SoundSource.AMBIENT);
        Minecraft.getInstance().getSoundManager().stop(
                ModSoundEvents.RECORDING_HEARTBEAT.get().getLocation(), SoundSource.AMBIENT);
        if (!wasActive) {
            return;
        }
        LOGGER.info("Synchronized in-game breathing and heartbeat audio stopped");
    }

    private static void playBreath() {
        float volume = (float) (Config.RECORDING_AUDIO_VOLUME.get() * 0.55D);
        if (volume <= 0.0F) {
            return;
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(
                ModSoundEvents.RECORDING_BREATH.get(), 1.0F, volume));
    }

    private static void playHeartbeat(double strength) {
        float volume = (float) (Config.RECORDING_AUDIO_VOLUME.get() * 0.85D * strength);
        if (volume <= 0.0F) {
            return;
        }

        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forLocalAmbience(
                ModSoundEvents.RECORDING_HEARTBEAT.get(), 1.0F, volume));
    }

    private static boolean crossedPhase(double previous, double current, double target) {
        if (previous <= current) {
            return previous < target && target <= current;
        }
        return target > previous || target <= current;
    }

}
