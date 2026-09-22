package com.example.bddmod.client.event;

import com.example.bddmod.Config;
import com.example.bddmod.client.BDDSessionData;
import com.example.bddmod.client.AudienceHeadEffectController;
import com.example.bddmod.client.AudienceRenderTargetManager;
import com.example.bddmod.client.AudienceWindowManager;
import com.example.bddmod.client.OBSMonitor;
import com.example.bddmod.client.OBSSetupScreen;
import com.example.bddmod.client.RecordingPulseController;
import com.example.bddmod.client.RecordingVeinOverlay;
import com.example.bddmod.client.RenderRouteTestRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.GameShuttingDownEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

/**
 * 客户端最小可玩层：提供状态 HUD、录制时的边缘反馈和独立 FBO 分流诊断面板。
 */
@Mod.EventBusSubscriber(modid = "bddmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEventHandler {
    private static final int HUD_COLOR = 0xD9E8E8E8;
    private static final int ACCENT_COLOR = 0xFFE07070;
    private static long clientTicks;
    private static boolean setupScreenShown;

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        clientTicks++;
        AudienceHeadEffectController.beginFrame();
        boolean recording = OBSMonitor.isRecording();
        RecordingPulseController.update(recording && Config.TERROR_MODE_ENABLED.get());
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null) {
            BDDSessionData.get().tick(recording, minecraft.screen == null);
        }
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        releaseAudienceResources();
    }

    @SubscribeEvent
    public static void onGameShuttingDown(GameShuttingDownEvent event) {
        releaseAudienceResources();
        OBSMonitor.stop();
    }

    private static void releaseAudienceResources() {
        AudienceWindowManager.release();
        AudienceRenderTargetManager.release();
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        AudienceHeadEffectController.captureLocalPlayerHead(event.getEntity(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (setupScreenShown
                || Config.OBS_SETUP_COMPLETED.get()
                || !(event.getNewScreen() instanceof TitleScreen)) {
            return;
        }

        setupScreenShown = true;
        event.setNewScreen(new OBSSetupScreen(event.getNewScreen(), true));
    }

    @SubscribeEvent
    public static void onScreenInitialized(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof TitleScreen titleScreen)) {
            return;
        }

        int x = titleScreen.width / 2 + 128;
        int y = titleScreen.height / 4 + 132;
        event.addListener(Button.builder(Component.literal("●").withStyle(ChatFormatting.RED), button ->
                        Minecraft.getInstance().setScreen(new OBSSetupScreen(titleScreen)))
                .bounds(x, y, 20, 20)
                .tooltip(Tooltip.create(Component.literal("OBS 设置")))
                .build());
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("route_test_diagnostic", (gui, graphics, partialTick, width, height) -> {
            Minecraft minecraft = Minecraft.getInstance();
            RenderRouteTestRenderer.render(graphics, minecraft, OBSMonitor.isRecording());
        });
    }

    @SubscribeEvent
    public static void onRenderGui(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        if (!Config.TERROR_MODE_ENABLED.get()) {
            return;
        }

        BDDSessionData session = BDDSessionData.get();
        boolean recording = OBSMonitor.isRecording();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        if (recording) {
            RecordingVeinOverlay.render(graphics, width, height, RecordingPulseController.visualLevel());
        }

        String state = recording ? "OBS RECORDING" : "OBS STANDBY";
        int stateColor = recording ? ACCENT_COLOR : HUD_COLOR;
        graphics.drawString(minecraft.font, Component.literal("BDD // " + state), 8, 8, stateColor, true);

        String stats = String.format(Locale.ROOT, "checks %d  covered %s", session.checks(), formatTicks(session.coveredTicks()));
        graphics.drawString(minecraft.font, Component.literal(stats), 8, 20, HUD_COLOR, true);
    }

    private static String formatTicks(long ticks) {
        long seconds = ticks / 20L;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L);
    }
}
