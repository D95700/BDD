package com.example.bddmod.client;

import com.example.bddmod.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;

/**
 * 可从首次启动流程或主菜单打开的本机 OBS WebSocket 设置页。
 */
public final class OBSSetupScreen extends Screen {
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 276;
    private static final int PANEL_COLOR = 0xF0181B20;
    private static final int BORDER_COLOR = 0xFF56606B;
    private static final int LABEL_COLOR = 0xFFE3E7EB;
    private static final int MUTED_COLOR = 0xFF9AA5B1;
    private static final int ERROR_COLOR = 0xFFFF7777;

    @Nullable
    private final Screen parent;
    private final boolean firstRun;
    private EditBox portBox;
    private EditBox passwordBox;
    private String validationMessage = "";

    public OBSSetupScreen(@Nullable Screen parent) {
        this(parent, false);
    }

    public OBSSetupScreen(@Nullable Screen parent, boolean firstRun) {
        super(Component.literal("OBS 设置引导"));
        this.parent = parent;
        this.firstRun = firstRun;
    }

    @Override
    protected void init() {
        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        int fieldLeft = left + 132;
        int fieldWidth = PANEL_WIDTH - 154;

        this.portBox = new EditBox(this.font, fieldLeft, top + 98, fieldWidth, 20,
                Component.literal("OBS 端口"));
        this.portBox.setMaxLength(5);
        this.portBox.setFilter(value -> value.matches("\\d{0,5}"));
        this.portBox.setValue(Integer.toString(Config.OBS_PORT.get()));
        this.addRenderableWidget(this.portBox);

        this.passwordBox = new EditBox(this.font, fieldLeft, top + 138, fieldWidth, 20,
                Component.literal("OBS 密码"));
        this.passwordBox.setMaxLength(256);
        this.passwordBox.setFormatter((value, cursor) ->
                FormattedCharSequence.forward("*".repeat(value.length()), net.minecraft.network.chat.Style.EMPTY));
        this.passwordBox.setValue(Config.OBS_WEBSOCKET_PASSWORD.get());
        this.passwordBox.setHint(Component.literal("留空表示未启用认证"));
        this.addRenderableWidget(this.passwordBox);

        this.addRenderableWidget(Button.builder(Component.literal("保存并连接"), button -> saveAndClose())
                .bounds(left + 28, top + 222, 150, 20)
                .build());
        Component closeLabel = Component.literal(this.firstRun ? "稍后设置" : "返回主菜单");
        this.addRenderableWidget(Button.builder(closeLabel, button -> closeWithoutSaving())
                .bounds(left + 182, top + 222, 150, 20)
                .build());
        this.setInitialFocus(this.portBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int left = (this.width - PANEL_WIDTH) / 2;
        int top = (this.height - PANEL_HEIGHT) / 2;
        graphics.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + PANEL_HEIGHT + 1, BORDER_COLOR);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, PANEL_COLOR);

        graphics.drawCenteredString(this.font, this.title, this.width / 2, top + 18, 0xFFFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("启动 OBS WebSocket 5 后填写以下设置"),
                this.width / 2, top + 40, MUTED_COLOR);
        graphics.drawString(this.font, Component.literal("连接地址"), left + 24, top + 75, LABEL_COLOR);
        graphics.drawString(this.font, Component.literal("127.0.0.1"), left + 132, top + 75, 0xFF8BD5FF);
        graphics.drawString(this.font, Component.literal("端口"), left + 24, top + 104, LABEL_COLOR);
        graphics.drawString(this.font, Component.literal("密码"), left + 24, top + 144, LABEL_COLOR);
        graphics.drawString(this.font, Component.literal("密码只保存到本机客户端配置，日志不会显示密码。"),
                left + 24, top + 180, MUTED_COLOR);
        graphics.drawString(this.font, Component.literal("OBS 设置：工具 > WebSocket 服务器设置"),
                left + 24, top + 196, MUTED_COLOR);

        if (!this.validationMessage.isEmpty()) {
            graphics.drawCenteredString(this.font, Component.literal(this.validationMessage),
                    this.width / 2, top + 210, ERROR_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }

    private void saveAndClose() {
        int port;
        try {
            port = Integer.parseInt(this.portBox.getValue());
        } catch (NumberFormatException exception) {
            this.validationMessage = "请输入 1 到 65535 之间的端口";
            return;
        }

        if (port < 1 || port > 65535) {
            this.validationMessage = "请输入 1 到 65535 之间的端口";
            return;
        }

        Config.OBS_PORT.set(port);
        Config.OBS_WEBSOCKET_PASSWORD.set(this.passwordBox.getValue());
        Config.OBS_SETUP_COMPLETED.set(true);
        Config.SPEC.save();
        OBSMonitor.reconnect();
        closeToParent();
    }

    private void closeWithoutSaving() {
        if (this.firstRun) {
            Config.OBS_SETUP_COMPLETED.set(true);
            Config.SPEC.save();
        }
        closeToParent();
    }

    private void closeToParent() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
