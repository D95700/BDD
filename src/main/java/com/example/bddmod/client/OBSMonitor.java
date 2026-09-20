package com.example.bddmod.client;

import com.example.bddmod.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OBS WebSocket 5 状态监视器。
 *
 * <p>使用 Java 17 自带的 WebSocket 客户端实现 OBS v5 握手、认证和录制状态事件，
 * 避免把 Jetty 等额外运行时依赖塞进模组 JAR。连接线程完全独立于 Minecraft 主线程，
 * OBS 不可用、认证失败或协议错误时只回退为未录制状态。</p>
 */
public final class OBSMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger("bddmod-obs-monitor");
    private static final String OBS_HOST = "127.0.0.1";
    private static final int OUTPUTS_SUBSCRIPTION = 1 << 6;
    private static final long RECONNECT_SECONDS = 5L;
    private static final long AUTH_FAILURE_RETRY_SECONDS = 30L;

    private static final AtomicBoolean RECORDING = new AtomicBoolean(false);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean CONNECTING = new AtomicBoolean(false);
    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);
    private static final AtomicBoolean AUTH_FAILED = new AtomicBoolean(false);
    private static final HttpClient WEBSOCKET_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private static ScheduledExecutorService executor;
    private static volatile WebSocket socket;
    private static volatile long authFailureTimeNanos;

    private OBSMonitor() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "bddmod-obs-monitor");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleWithFixedDelay(OBSMonitor::connectIfNeeded, 1L, RECONNECT_SECONDS, TimeUnit.SECONDS);
    }

    public static boolean isRecording() {
        return RECORDING.get();
    }

    public static String getConnectionStatus() {
        if (CONNECTED.get()) {
            return "CONNECTED";
        }
        if (AUTH_FAILED.get()) {
            return "AUTH FAILED";
        }
        if (CONNECTING.get()) {
            return "CONNECTING";
        }
        return "DISCONNECTED";
    }

    /**
     * 由设置界面调用，在后台丢弃旧连接并立即开始一次新的连接尝试。
     */
    public static void reconnect() {
        ScheduledExecutorService currentExecutor = executor;
        if (currentExecutor == null) {
            return;
        }

        currentExecutor.execute(() -> {
            WebSocket previousSocket = socket;
            socket = null;
            CONNECTING.set(false);
            AUTH_FAILED.set(false);
            authFailureTimeNanos = 0L;
            clearState();
            if (previousSocket != null) {
                previousSocket.sendClose(WebSocket.NORMAL_CLOSURE, "settings changed")
                        .exceptionally(ignored -> null);
            }
            connectIfNeeded();
        });
    }

    private static void connectIfNeeded() {
        if (socket != null || !CONNECTING.compareAndSet(false, true)) {
            return;
        }

        if (AUTH_FAILED.get()
                && System.nanoTime() - authFailureTimeNanos < TimeUnit.SECONDS.toNanos(AUTH_FAILURE_RETRY_SECONDS)) {
            CONNECTING.set(false);
            return;
        }

        AUTH_FAILED.set(false);

        try {
            URI endpoint = URI.create("ws://" + OBS_HOST + ":" + Config.OBS_PORT.get());
            WEBSOCKET_CLIENT.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .buildAsync(endpoint, new Listener())
                    .whenComplete((webSocket, error) -> {
                        CONNECTING.set(false);
                        if (error != null) {
                            clearState();
                            LOGGER.debug("OBS WebSocket connection attempt failed: {}", error.toString());
                        } else {
                            socket = webSocket;
                        }
                    });
        } catch (Exception ignored) {
            CONNECTING.set(false);
            clearState();
        }
    }

    private static void clearState() {
        RECORDING.set(false);
        CONNECTED.set(false);
        socket = null;
    }

    private static void handleMessage(String text, WebSocket webSocket) {
        try {
            JsonObject message = JsonParser.parseString(text).getAsJsonObject();
            int operationCode = message.get("op").getAsInt();
            JsonObject data = message.has("d") && message.get("d").isJsonObject()
                    ? message.getAsJsonObject("d")
                    : new JsonObject();

            switch (operationCode) {
                case 0 -> sendIdentify(webSocket, data);
                case 2 -> requestRecordStatus(webSocket);
                case 5 -> handleEvent(data);
                case 7 -> handleRequestResponse(data);
                default -> {
                    // OBS 的其他握手、请求或事件消息对录制状态没有影响。
                }
            }
        } catch (Exception exception) {
            LOGGER.debug("OBS WebSocket message handling failed: {}", exception.toString());
            // 单条损坏消息不应影响客户端；连接若已失效会由 onClose 触发重连。
        }
    }

    private static void sendIdentify(WebSocket webSocket, JsonObject helloData) {
        JsonObject identifyData = new JsonObject();
        identifyData.addProperty("rpcVersion", 1);
        identifyData.addProperty("eventSubscriptions", OUTPUTS_SUBSCRIPTION);

        if (helloData.has("authentication") && helloData.get("authentication").isJsonObject()) {
            JsonObject authentication = helloData.getAsJsonObject("authentication");
            String password = Config.OBS_WEBSOCKET_PASSWORD.get();
            String salt = authentication.get("salt").getAsString();
            String challenge = authentication.get("challenge").getAsString();
            identifyData.addProperty("authentication", computeAuthentication(password, salt, challenge));
        }

        send(webSocket, 1, identifyData);
    }

    private static void requestRecordStatus(WebSocket webSocket) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("requestType", "GetRecordStatus");
        requestData.addProperty("requestId", UUID.randomUUID().toString());
        requestData.add("requestData", new JsonObject());
        // OBS WebSocket 5 Request messages use operation code 6.
        send(webSocket, 6, requestData);
    }

    private static void handleEvent(JsonObject eventData) {
        if (!"RecordStateChanged".equals(eventData.has("eventType")
                ? eventData.get("eventType").getAsString() : "")) {
            return;
        }

        JsonObject data = eventData.has("eventData") && eventData.get("eventData").isJsonObject()
                ? eventData.getAsJsonObject("eventData")
                : new JsonObject();
        updateRecording(data);
    }

    private static void handleRequestResponse(JsonObject responseData) {
        if (!"GetRecordStatus".equals(responseData.has("requestType")
                ? responseData.get("requestType").getAsString() : "")) {
            return;
        }

        JsonObject status = responseData.has("requestStatus") && responseData.get("requestStatus").isJsonObject()
                ? responseData.getAsJsonObject("requestStatus")
                : new JsonObject();
        if (!status.has("result") || !status.get("result").getAsBoolean()) {
            LOGGER.debug("OBS GetRecordStatus request was rejected: {}", status.has("code")
                    ? status.get("code").getAsString() : "unknown");
            clearState();
            return;
        }

        JsonObject data = responseData.has("responseData") && responseData.get("responseData").isJsonObject()
                ? responseData.getAsJsonObject("responseData")
                : new JsonObject();
        updateRecording(data);
    }

    private static void updateRecording(JsonObject data) {
        boolean previous = RECORDING.get();
        if (data.has("outputActive")) {
            RECORDING.set(data.get("outputActive").getAsBoolean());
            logRecordingTransition(previous);
            return;
        }

        String outputState = data.has("outputState") ? data.get("outputState").getAsString() : "";
        RECORDING.set("OBS_WEBSOCKET_OUTPUT_STARTED".equals(outputState));
        logRecordingTransition(previous);
    }

    private static void logRecordingTransition(boolean previous) {
        boolean current = RECORDING.get();
        if (previous != current) {
            LOGGER.info("OBS recording state changed: {}", current ? "RECORDING" : "STANDBY");
        }
    }

    private static void send(WebSocket webSocket, int operationCode, JsonObject data) {
        JsonObject message = new JsonObject();
        message.addProperty("op", operationCode);
        message.add("d", data);
        webSocket.sendText(message.toString(), true);
    }

    private static String computeAuthentication(String password, String salt, String challenge) {
        String secret = sha256Base64(password + salt);
        return sha256Base64(secret + challenge);
    }

    private static String sha256Base64(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for OBS WebSocket authentication", exception);
        }
    }

    private static final class Listener implements WebSocket.Listener {
        private final StringBuilder messageBuffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            socket = webSocket;
            CONNECTED.set(true);
            LOGGER.info("OBS WebSocket connected to 127.0.0.1:{}", Config.OBS_PORT.get());
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            messageBuffer.append(data);
            if (last) {
                String message = messageBuffer.toString();
                messageBuffer.setLength(0);
                handleMessage(message, webSocket);
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            if (socket != webSocket) {
                return;
            }
            clearState();
            LOGGER.debug("OBS WebSocket error: {}", error.toString());
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            if (socket != webSocket) {
                return null;
            }
            LOGGER.debug("OBS WebSocket closed: code={}, reason={}", statusCode, reason);
            if (statusCode == 4009) {
                AUTH_FAILED.set(true);
                authFailureTimeNanos = System.nanoTime();
                LOGGER.warn("OBS WebSocket authentication failed; check obsWebSocketPassword in the client config");
            }
            clearState();
            CONNECTING.set(false);
            return null;
        }
    }
}
