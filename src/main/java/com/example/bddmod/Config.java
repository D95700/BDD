package com.example.bddmod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue TERROR_MODE_ENABLED = BUILDER
            .comment("启用客户端本地 BDD 视觉叠加")
            .define("terrorModeEnabled", true);
    public static final ForgeConfigSpec.BooleanValue RENDER_ROUTE_TEST_ENABLED = BUILDER
            .comment("录制时启用 FBO 与独立观众窗口诊断；该测试不包含 OBS 原生交换缓冲钩子")
            .define("renderRouteTestEnabled", false);
    public static final ForgeConfigSpec.DoubleValue RECORDING_AUDIO_VOLUME = BUILDER
            .comment("游戏内录制呼吸与心跳音量，0 到 1；同时受 Minecraft 主音量和环境音效音量控制")
            .defineInRange("recordingAudioVolume", 0.30D, 0.0D, 1.0D);
    public static final ForgeConfigSpec.ConfigValue<String> OBS_WEBSOCKET_PASSWORD = BUILDER
            .comment("OBS WebSocket 5 密码；留空表示 OBS 未启用密码")
            .define("obsWebSocketPassword", "");
    public static final ForgeConfigSpec.BooleanValue OBS_SETUP_COMPLETED = BUILDER
            .comment("是否已经完成 OBS 首次启动引导")
            .define("obsSetupCompleted", false);
    public static final ForgeConfigSpec.IntValue OBS_PORT = BUILDER
            .comment("仅允许访问本机 OBS WebSocket 端口")
            .defineInRange("obsPort", 4455, 1, 65535);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    private Config() {}
}
