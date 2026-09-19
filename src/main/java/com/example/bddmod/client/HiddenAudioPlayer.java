package com.example.bddmod.client;

import com.example.bddmod.Config;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class HiddenAudioPlayer {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "bddmod-hidden-audio");
        t.setDaemon(true);
        return t;
    });
    private HiddenAudioPlayer() {}

    public static void play(byte[] pcm, AudioFormat format) {
        if (pcm == null || pcm.length == 0 || Config.HIDDEN_AUDIO_VOLUME.get() <= 0) return;
        EXECUTOR.execute(() -> {
            String[] names = Config.VIRTUAL_AUDIO_DEVICE_NAME.get().split(",");
            for (Mixer.Info info : AudioSystem.getMixerInfo()) {
                String name = info.toString().toLowerCase(Locale.ROOT);
                if (!Arrays.stream(names).map(s -> s.trim().toLowerCase(Locale.ROOT)).anyMatch(name::contains)) continue;
                try {
                    SourceDataLine line = AudioSystem.getSourceDataLine(format, AudioSystem.getMixer(info).getMixerInfo());
                    line.open(format);
                    line.start();
                    line.write(pcm, 0, pcm.length);
                    line.drain();
                    line.close();
                } catch (Exception ignored) {
                    // 虚拟声卡不可用时静默回退，绝不改用默认输出设备。
                }
                return;
            }
        });
    }
}
