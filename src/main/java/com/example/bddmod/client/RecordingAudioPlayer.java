package com.example.bddmod.client;

import com.example.bddmod.Config;
import com.example.bddmod.sound.ModSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RecordingAudioPlayer {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-recording-audio");
    private static BreathingSound activeBreathing;
    private static double previousPhase = Double.NaN;

    private RecordingAudioPlayer() {
    }

    public static void start() {
        if (activeBreathing != null) {
            return;
        }

        BreathingSound breathing = new BreathingSound();
        activeBreathing = breathing;
        previousPhase = RecordingPulseController.phase();
        Minecraft.getInstance().getSoundManager().play(breathing);
        LOGGER.info("Synchronized in-game breathing and heartbeat audio started");
    }

    public static void tick() {
        if (!RecordingPulseController.isActive()) {
            stop();
            return;
        }

        double currentPhase = RecordingPulseController.phase();
        if (!Double.isNaN(previousPhase)) {
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
        BreathingSound breathing = activeBreathing;
        activeBreathing = null;
        previousPhase = Double.NaN;
        if (breathing != null) {
            breathing.stopPlayback();
            Minecraft.getInstance().getSoundManager().stop(breathing);
        }

        Minecraft.getInstance().getSoundManager().stop(
                ModSoundEvents.RECORDING_HEARTBEAT.get().getLocation(), SoundSource.AMBIENT);
        if (breathing == null) {
            return;
        }
        LOGGER.info("Synchronized in-game breathing and heartbeat audio stopped");
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

    private static final class BreathingSound extends AbstractTickableSoundInstance {
        private BreathingSound() {
            super(ModSoundEvents.RECORDING_BREATH.get(), SoundSource.AMBIENT, RandomSource.create());
            looping = true;
            delay = 0;
            attenuation = SoundInstance.Attenuation.NONE;
            relative = true;
            pitch = 1.0F;
            updateVolume();
        }

        @Override
        public void tick() {
            if (!RecordingPulseController.isActive() || activeBreathing != this) {
                stop();
                return;
            }
            updateVolume();
        }

        private void updateVolume() {
            double envelope = RecordingPulseController.breathEnvelopeAt(System.nanoTime());
            volume = (float) (Config.RECORDING_AUDIO_VOLUME.get() * (0.22D + envelope * 0.58D));
        }

        private void stopPlayback() {
            stop();
        }
    }
}
