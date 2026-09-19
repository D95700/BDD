package com.example.bddmod.client;

import java.time.Instant;
import java.util.Map;

public final class LocalInfoProvider {
    private LocalInfoProvider() {}

    // 只返回明确允许的非敏感系统属性，不读取网络、文件、剪贴板或设备标识。
    public static Map<String, String> snapshot() {
        return Map.of(
                "user", System.getProperty("user.name", "unknown"),
                "os", System.getProperty("os.name", "unknown"),
                "time", Instant.now().toString()
        );
    }
}
