package com.example.bddmod;

import net.minecraftforge.common.ForgeConfigSpec;

public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue TERROR_MODE_ENABLED = BUILDER
            .comment("启用客户端本地 BDD 视觉叠加")
            .define("terrorModeEnabled", true);
    public static final ForgeConfigSpec.DoubleValue HIDDEN_AUDIO_VOLUME = BUILDER
            .comment("虚拟音频设备播放音量，0 到 1")
            .defineInRange("hiddenAudioVolume", 0.35D, 0.0D, 1.0D);
    public static final ForgeConfigSpec.ConfigValue<String> OBS_WEBSOCKET_PASSWORD = BUILDER
            .comment("保留供可选 OBS 适配器使用；默认不发送密码")
            .define("obsWebSocketPassword", "");
    public static final ForgeConfigSpec.ConfigValue<String> VIRTUAL_AUDIO_DEVICE_NAME = BUILDER
            .comment("Java Sound 设备名称匹配关键词")
            .define("virtualAudioDeviceName", "VB-Audio,Voicemeeter,CABLE Input");
    public static final ForgeConfigSpec.IntValue OBS_PORT = BUILDER
            .comment("仅允许访问本机 OBS WebSocket 端口")
            .defineInRange("obsPort", 4455, 1, 65535);
    public static final ForgeConfigSpec SPEC = BUILDER.build();
    private Config() {}
}
