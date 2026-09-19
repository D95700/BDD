package com.example.bddmod.client;

import com.example.bddmod.Config;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class OBSMonitor {
    private static final AtomicBoolean RECORDING = new AtomicBoolean(false);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static ScheduledExecutorService executor;
    private OBSMonitor() {}

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "bddmod-obs-monitor");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleWithFixedDelay(OBSMonitor::pollSafely, 1, 2, TimeUnit.SECONDS);
    }

    public static boolean isRecording() { return RECORDING.get(); }

    private static void pollSafely() {
        try {
            int port = Config.OBS_PORT.get();
            boolean reachable = portOpen(port);
            if (!reachable) {
                RECORDING.set(false);
                return;
            }
            HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/"))
                    .timeout(Duration.ofMillis(350)).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body().toLowerCase(java.util.Locale.ROOT);
            RECORDING.set(body.contains("recording") && (body.contains("true") || body.contains("started")));
        } catch (Exception ignored) {
            // OBS 未运行、鉴权失败或协议不匹配都不应打断客户端。
            RECORDING.set(false);
        }
    }

    private static boolean portOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 250);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }
}
