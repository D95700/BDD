package com.example.bddmod.client;

import com.example.bddmod.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
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
    private static final int CONNECTION_TIMEOUT_MILLIS = 2_000;
    private static final int OUTPUTS_SUBSCRIPTION = 1 << 6;
    private static final long RECONNECT_SECONDS = 5L;
    private static final long AUTH_FAILURE_RETRY_SECONDS = 30L;
    private static final String GAME_CAPTURE_KIND = "game_capture";
    private static final String WINDOW_CAPTURE_KIND = "window_capture";

    private static final AtomicBoolean RECORDING = new AtomicBoolean(false);
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static final AtomicBoolean CONNECTING = new AtomicBoolean(false);
    private static final AtomicBoolean CONNECTED = new AtomicBoolean(false);
    private static final AtomicBoolean AUTH_FAILED = new AtomicBoolean(false);
    private static final AtomicBoolean AUDIENCE_ROUTE_IN_FLIGHT = new AtomicBoolean(false);
    private static final AtomicBoolean AUDIENCE_ROUTE_APPLIED = new AtomicBoolean(false);
    private static final AtomicInteger PENDING_CAPTURE_SETTINGS = new AtomicInteger();
    private static final AtomicLong CONNECTION_GENERATION = new AtomicLong();
    private static final Map<String, String> CAPTURE_SETTINGS_REQUESTS = new ConcurrentHashMap<>();
    private static final Map<String, String> CAPTURE_ROUTE_REQUESTS = new ConcurrentHashMap<>();
    private static final Set<String> MATCHING_CAPTURE_INPUTS = ConcurrentHashMap.newKeySet();
    private static ScheduledExecutorService executor;
    private static volatile WebSocket pendingSocket;
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

    public static boolean isConnected() {
        return CONNECTED.get();
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

    public static void requestAudienceCaptureRoute() {
        ScheduledExecutorService currentExecutor = executor;
        if (currentExecutor == null || !Config.TERROR_MODE_ENABLED.get()
                || !Config.AUTO_ROUTE_AUDIENCE_CAPTURE.get()) {
            return;
        }
        currentExecutor.execute(() -> beginAudienceCaptureRoute(socket));
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
            WebSocket previousPendingSocket = pendingSocket;
            CONNECTION_GENERATION.incrementAndGet();
            socket = null;
            pendingSocket = null;
            CONNECTING.set(false);
            AUTH_FAILED.set(false);
            authFailureTimeNanos = 0L;
            clearState();
            disconnectQuietly(previousSocket, "settings changed");
            if (previousPendingSocket != previousSocket) {
                disconnectQuietly(previousPendingSocket, "settings changed");
            }
            connectIfNeeded();
        });
    }

    private static void connectIfNeeded() {
        if (socket != null || pendingSocket != null || !CONNECTING.compareAndSet(false, true)) {
            return;
        }

        if (AUTH_FAILED.get()
                && System.nanoTime() - authFailureTimeNanos < TimeUnit.SECONDS.toNanos(AUTH_FAILURE_RETRY_SECONDS)) {
            CONNECTING.set(false);
            return;
        }

        AUTH_FAILED.set(false);
        long generation = CONNECTION_GENERATION.incrementAndGet();

        try {
            String endpoint = "ws://" + OBS_HOST + ":" + Config.OBS_PORT.get();
            WebSocket candidate = new WebSocketFactory()
                    .setConnectionTimeout(CONNECTION_TIMEOUT_MILLIS)
                    .createSocket(endpoint)
                    .addListener(new Listener(generation));
            pendingSocket = candidate;
            candidate.connectAsynchronously();
        } catch (Exception exception) {
            if (generation == CONNECTION_GENERATION.get()) {
                pendingSocket = null;
                CONNECTING.set(false);
                clearState();
            }
            LOGGER.debug("OBS WebSocket connection setup failed: {}", exception.toString());
        }
    }

    private static void clearState() {
        RECORDING.set(false);
        CONNECTED.set(false);
        socket = null;
        resetAudienceRouting();
    }

    private static void resetAudienceRouting() {
        AUDIENCE_ROUTE_IN_FLIGHT.set(false);
        AUDIENCE_ROUTE_APPLIED.set(false);
        PENDING_CAPTURE_SETTINGS.set(0);
        CAPTURE_SETTINGS_REQUESTS.clear();
        CAPTURE_ROUTE_REQUESTS.clear();
        MATCHING_CAPTURE_INPUTS.clear();
    }

    private static void disconnectQuietly(WebSocket webSocket, String reason) {
        if (webSocket == null) {
            return;
        }
        try {
            webSocket.disconnect(reason);
        } catch (RuntimeException exception) {
            LOGGER.debug("OBS WebSocket disconnect failed: {}", exception.toString());
        }
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
                case 2 -> handleIdentified(webSocket);
                case 5 -> handleEvent(data);
                case 7 -> handleRequestResponse(data, webSocket);
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

    private static void handleIdentified(WebSocket webSocket) {
        if (socket != webSocket) {
            return;
        }
        CONNECTING.set(false);
        CONNECTED.set(true);
        LOGGER.info("OBS WebSocket identified on 127.0.0.1:{}", Config.OBS_PORT.get());
        requestRecordStatus(webSocket);
    }

    private static void requestRecordStatus(WebSocket webSocket) {
        sendRequest(webSocket, "GetRecordStatus", new JsonObject());
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

    private static void handleRequestResponse(JsonObject responseData, WebSocket webSocket) {
        String requestType = responseData.has("requestType")
                ? responseData.get("requestType").getAsString() : "";
        String requestId = responseData.has("requestId")
                ? responseData.get("requestId").getAsString() : "";
        JsonObject status = responseData.has("requestStatus") && responseData.get("requestStatus").isJsonObject()
                ? responseData.getAsJsonObject("requestStatus")
                : new JsonObject();
        if (!status.has("result") || !status.get("result").getAsBoolean()) {
            handleRejectedRequest(requestType, requestId, status, webSocket);
            return;
        }

        JsonObject data = responseData.has("responseData") && responseData.get("responseData").isJsonObject()
                ? responseData.getAsJsonObject("responseData")
                : new JsonObject();
        switch (requestType) {
            case "GetRecordStatus" -> updateRecording(data);
            case "GetCurrentProgramScene" -> handleCurrentProgramScene(data, webSocket);
            case "GetSceneItemList" -> handleSceneItemList(data, webSocket);
            case "GetInputSettings" -> handleCaptureInputSettings(requestId, data, webSocket);
            case "SetInputSettings" -> handleCaptureRouteApplied(requestId);
            default -> {
                // Responses outside the monitor and audience-routing workflow are ignored.
            }
        }
    }

    private static void handleRejectedRequest(String requestType, String requestId, JsonObject status,
                                              WebSocket webSocket) {
        String code = status.has("code") ? status.get("code").getAsString() : "unknown";
        if ("GetRecordStatus".equals(requestType)) {
            LOGGER.debug("OBS GetRecordStatus request was rejected: {}", code);
            clearState();
            return;
        }

        if ("GetInputSettings".equals(requestType)) {
            String inputName = CAPTURE_SETTINGS_REQUESTS.remove(requestId);
            if (inputName != null) {
                finishCaptureSettingsInspection(null);
                completeCaptureSettingsInspection(webSocket);
            }
        } else {
            CAPTURE_ROUTE_REQUESTS.remove(requestId);
            AUDIENCE_ROUTE_IN_FLIGHT.set(false);
        }
        LOGGER.warn("OBS audience capture routing request {} was rejected with code {}", requestType, code);
    }

    private static void beginAudienceCaptureRoute(WebSocket webSocket) {
        if (webSocket == null || !CONNECTED.get() || !Config.TERROR_MODE_ENABLED.get()
                || !Config.AUTO_ROUTE_AUDIENCE_CAPTURE.get()
                || AUDIENCE_ROUTE_APPLIED.get() || !AUDIENCE_ROUTE_IN_FLIGHT.compareAndSet(false, true)) {
            return;
        }
        sendRequest(webSocket, "GetCurrentProgramScene", new JsonObject());
    }

    private static void handleCurrentProgramScene(JsonObject data, WebSocket webSocket) {
        String sceneName = data.has("currentProgramSceneName")
                ? data.get("currentProgramSceneName").getAsString() : "";
        if (sceneName.isBlank()) {
            LOGGER.warn("OBS audience capture routing skipped because the current program scene is unavailable");
            AUDIENCE_ROUTE_IN_FLIGHT.set(false);
            return;
        }

        JsonObject requestData = new JsonObject();
        requestData.addProperty("sceneName", sceneName);
        sendRequest(webSocket, "GetSceneItemList", requestData);
    }

    private static void handleSceneItemList(JsonObject data, WebSocket webSocket) {
        JsonArray sceneItems = data.has("sceneItems") && data.get("sceneItems").isJsonArray()
                ? data.getAsJsonArray("sceneItems") : new JsonArray();
        Set<String> candidates = new HashSet<>();
        for (JsonElement element : sceneItems) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            boolean enabled = !item.has("sceneItemEnabled") || item.get("sceneItemEnabled").getAsBoolean();
            String inputKind = item.has("inputKind") ? item.get("inputKind").getAsString() : "";
            String sourceName = item.has("sourceName") ? item.get("sourceName").getAsString() : "";
            if (enabled && !sourceName.isBlank() && isCaptureInputKind(inputKind)) {
                candidates.add(sourceName);
            }
        }

        if (candidates.isEmpty()) {
            LOGGER.warn("OBS audience capture routing found no enabled game/window capture source in the current scene");
            AUDIENCE_ROUTE_IN_FLIGHT.set(false);
            return;
        }
        if (candidates.size() == 1) {
            setAudienceCaptureInput(webSocket, candidates.iterator().next());
            return;
        }

        MATCHING_CAPTURE_INPUTS.clear();
        PENDING_CAPTURE_SETTINGS.set(candidates.size());
        for (String inputName : candidates) {
            JsonObject requestData = new JsonObject();
            requestData.addProperty("inputName", inputName);
            String requestId = UUID.randomUUID().toString();
            CAPTURE_SETTINGS_REQUESTS.put(requestId, inputName);
            sendRequest(webSocket, "GetInputSettings", requestData, requestId);
        }
    }

    private static void handleCaptureInputSettings(String requestId, JsonObject data, WebSocket webSocket) {
        String inputName = CAPTURE_SETTINGS_REQUESTS.remove(requestId);
        if (inputName == null) {
            return;
        }

        JsonObject settings = data.has("inputSettings") && data.get("inputSettings").isJsonObject()
                ? data.getAsJsonObject("inputSettings") : new JsonObject();
        String selectedWindow = settings.has("window") ? settings.get("window").getAsString() : "";
        String searchable = (inputName + " " + selectedWindow).toLowerCase(Locale.ROOT);
        String match = searchable.contains("minecraft") || searchable.contains("java.exe")
                || searchable.contains("javaw.exe") || searchable.contains("bdd audience output")
                ? inputName : null;
        finishCaptureSettingsInspection(match);
        completeCaptureSettingsInspection(webSocket);
    }

    private static void finishCaptureSettingsInspection(String matchingInput) {
        if (matchingInput != null) {
            MATCHING_CAPTURE_INPUTS.add(matchingInput);
        }
        PENDING_CAPTURE_SETTINGS.updateAndGet(value -> Math.max(0, value - 1));
    }

    private static void completeCaptureSettingsInspection(WebSocket webSocket) {
        if (PENDING_CAPTURE_SETTINGS.get() != 0) {
            return;
        }
        if (MATCHING_CAPTURE_INPUTS.size() == 1) {
            setAudienceCaptureInput(webSocket, MATCHING_CAPTURE_INPUTS.iterator().next());
        } else {
            LOGGER.warn("OBS audience capture routing left multiple capture sources unchanged because {} matched Minecraft",
                    MATCHING_CAPTURE_INPUTS.size());
            AUDIENCE_ROUTE_IN_FLIGHT.set(false);
        }
    }

    private static boolean isCaptureInputKind(String inputKind) {
        return GAME_CAPTURE_KIND.equals(inputKind)
                || WINDOW_CAPTURE_KIND.equals(inputKind)
                || inputKind.startsWith(WINDOW_CAPTURE_KIND + "_");
    }

    private static void setAudienceCaptureInput(WebSocket webSocket, String inputName) {
        JsonObject inputSettings = new JsonObject();
        inputSettings.addProperty("window", AudienceWindowManager.captureSelector());
        JsonObject requestData = new JsonObject();
        requestData.addProperty("inputName", inputName);
        requestData.add("inputSettings", inputSettings);
        requestData.addProperty("overlay", true);
        String requestId = UUID.randomUUID().toString();
        CAPTURE_ROUTE_REQUESTS.put(requestId, inputName);
        sendRequest(webSocket, "SetInputSettings", requestData, requestId);
    }

    private static void handleCaptureRouteApplied(String requestId) {
        String inputName = CAPTURE_ROUTE_REQUESTS.remove(requestId);
        if (inputName == null) {
            return;
        }
        AUDIENCE_ROUTE_APPLIED.set(true);
        AUDIENCE_ROUTE_IN_FLIGHT.set(false);
        LOGGER.info("OBS capture input '{}' now targets '{}'", inputName, AudienceWindowManager.captureSelector());
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
        webSocket.sendText(message.toString());
    }

    private static String sendRequest(WebSocket webSocket, String requestType, JsonObject requestPayload) {
        String requestId = UUID.randomUUID().toString();
        sendRequest(webSocket, requestType, requestPayload, requestId);
        return requestId;
    }

    private static void sendRequest(WebSocket webSocket, String requestType, JsonObject requestPayload,
                                    String requestId) {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("requestType", requestType);
        requestData.addProperty("requestId", requestId);
        requestData.add("requestData", requestPayload);
        // OBS WebSocket 5 Request messages use operation code 6.
        send(webSocket, 6, requestData);
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

    private static final class Listener extends WebSocketAdapter {
        private final long generation;

        private Listener(long generation) {
            this.generation = generation;
        }

        @Override
        public void onConnected(WebSocket webSocket, Map<String, List<String>> headers) {
            if (!isCurrent(webSocket)) {
                disconnectQuietly(webSocket, "obsolete connection");
                return;
            }
            pendingSocket = null;
            socket = webSocket;
            LOGGER.debug("OBS WebSocket transport connected to 127.0.0.1:{}", Config.OBS_PORT.get());
        }

        @Override
        public void onTextMessage(WebSocket webSocket, String text) {
            if (generation == CONNECTION_GENERATION.get() && socket == webSocket) {
                handleMessage(text, webSocket);
            }
        }

        @Override
        public void onConnectError(WebSocket webSocket, WebSocketException exception) {
            if (!isCurrent(webSocket)) {
                return;
            }
            pendingSocket = null;
            CONNECTING.set(false);
            clearState();
            LOGGER.debug("OBS WebSocket connection attempt failed: {}", exception.toString());
        }

        @Override
        public void onError(WebSocket webSocket, WebSocketException exception) {
            if (isCurrent(webSocket)) {
                LOGGER.debug("OBS WebSocket error: {}", exception.toString());
            }
        }

        @Override
        public void onDisconnected(WebSocket webSocket, WebSocketFrame serverCloseFrame,
                                   WebSocketFrame clientCloseFrame, boolean closedByServer) {
            if (!isCurrent(webSocket)) {
                return;
            }

            WebSocketFrame closeFrame = serverCloseFrame != null ? serverCloseFrame : clientCloseFrame;
            int statusCode = closeFrame != null ? closeFrame.getCloseCode() : 1006;
            String reason = closeFrame != null && closeFrame.getCloseReason() != null
                    ? closeFrame.getCloseReason() : "connection lost";
            LOGGER.debug("OBS WebSocket closed: code={}, reason={}", statusCode, reason);
            if (statusCode == 4009) {
                AUTH_FAILED.set(true);
                authFailureTimeNanos = System.nanoTime();
                LOGGER.warn("OBS WebSocket authentication failed; check obsWebSocketPassword in the client config");
            }
            pendingSocket = null;
            clearState();
            CONNECTING.set(false);
        }

        private boolean isCurrent(WebSocket webSocket) {
            return generation == CONNECTION_GENERATION.get()
                    && (pendingSocket == webSocket || socket == webSocket);
        }
    }
}
