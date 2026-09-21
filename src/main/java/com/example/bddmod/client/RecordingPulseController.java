package com.example.bddmod.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared clock for recording-time visuals and heartbeat audio.
 */
public final class RecordingPulseController {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-recording-pulse");
    private static final double CYCLE_SECONDS = 3.0D;
    private static final long CYCLE_NANOS = (long) (CYCLE_SECONDS * 1_000_000_000L);
    private static final double[] HEARTBEAT_PHASES = {0.08D, 0.15D, 0.58D, 0.65D};
    private static final double[] HEARTBEAT_STRENGTHS = {1.0D, 0.62D, 0.88D, 0.55D};

    private static volatile boolean active;
    private static volatile long cycleStartNanos;

    private RecordingPulseController() {
    }

    public static void update(boolean shouldBeActive) {
        if (shouldBeActive == active) {
            if (active) {
                RecordingAudioPlayer.tick();
            }
            return;
        }

        if (shouldBeActive) {
            cycleStartNanos = System.nanoTime();
            active = true;
            RecordingAudioPlayer.start();
            LOGGER.info("Synchronized recording pulse started");
        } else {
            active = false;
            RecordingAudioPlayer.stop();
            LOGGER.info("Synchronized recording pulse stopped");
        }
    }

    public static double visualLevel() {
        if (!active) {
            return 0.0D;
        }

        long now = System.nanoTime();
        double breath = breathEnvelopeAt(now);
        double heartbeat = heartbeatEnvelopeAt(now);
        return clamp(0.08D + breath * 0.52D + heartbeat * 0.40D);
    }

    static boolean isActive() {
        return active;
    }

    static double phase() {
        return phaseAt(System.nanoTime());
    }

    static int heartbeatCount() {
        return HEARTBEAT_PHASES.length;
    }

    static double heartbeatPhase(int index) {
        return HEARTBEAT_PHASES[index];
    }

    static double heartbeatStrength(int index) {
        return HEARTBEAT_STRENGTHS[index];
    }

    static double breathEnvelopeAt(long timeNanos) {
        double phase = phaseAt(timeNanos);
        if (phase < 0.22D) {
            return Math.pow(Math.sin(Math.PI * phase / 0.22D), 1.2D) * 0.72D;
        }
        if (phase >= 0.29D && phase < 0.72D) {
            double exhalePhase = (phase - 0.29D) / 0.43D;
            return Math.pow(Math.sin(Math.PI * exhalePhase), 0.85D);
        }
        return 0.0D;
    }

    static double heartbeatEnvelopeAt(long timeNanos) {
        double phase = phaseAt(timeNanos);
        double envelope = 0.0D;
        for (int index = 0; index < HEARTBEAT_PHASES.length; index++) {
            double elapsed = elapsedSincePulse(phase, HEARTBEAT_PHASES[index]);
            if (elapsed < 0.18D) {
                envelope = Math.max(envelope, HEARTBEAT_STRENGTHS[index] * Math.exp(-elapsed * 24.0D));
            }
        }
        return envelope;
    }

    private static double phaseAt(long timeNanos) {
        long elapsed = Math.floorMod(timeNanos - cycleStartNanos, CYCLE_NANOS);
        return elapsed / (double) CYCLE_NANOS;
    }

    private static double elapsedSincePulse(double phase, double pulsePhase) {
        double elapsedPhase = phase - pulsePhase;
        if (elapsedPhase < 0.0D) {
            elapsedPhase += 1.0D;
        }
        return elapsedPhase * CYCLE_SECONDS;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
