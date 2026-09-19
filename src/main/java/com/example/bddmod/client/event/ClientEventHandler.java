package com.example.bddmod.client.event;

import com.example.bddmod.Config;
import com.example.bddmod.client.BDDSessionData;
import com.example.bddmod.client.OBSMonitor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;

/**
 * 客户端最小可玩层：提供状态 HUD 和 OBS 录制时的轻微边缘视觉反馈。
 * 复杂的 FBO/Mixin 分流留到后续迭代，避免影响基础游戏稳定性。
 */
@Mod.EventBusSubscriber(modid = "bddmod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientEventHandler {
    private static final int HUD_COLOR = 0xD9E8E8E8;
    private static final int ACCENT_COLOR = 0xFFE07070;
    private static long clientTicks;

    private ClientEventHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        clientTicks++;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null) {
            BDDSessionData.get().tick(OBSMonitor.isRecording(), minecraft.screen == null);
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || !Config.TERROR_MODE_ENABLED.get()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        BDDSessionData session = BDDSessionData.get();
        boolean recording = OBSMonitor.isRecording();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        String state = recording ? "OBS RECORDING" : "OBS STANDBY";
        int stateColor = recording ? ACCENT_COLOR : HUD_COLOR;
        graphics.drawString(minecraft.font, Component.literal("BDD // " + state), 8, 8, stateColor, true);

        String stats = String.format(Locale.ROOT, "checks %d  covered %s", session.checks(), formatTicks(session.coveredTicks()));
        graphics.drawString(minecraft.font, Component.literal(stats), 8, 20, HUD_COLOR, true);

        if (recording) {
            int alpha = 28 + (int) (12 * Math.sin(clientTicks / 8.0D));
            int overlay = (Math.max(0, Math.min(255, alpha)) << 24) | 0x5A1010;
            graphics.fill(0, 0, width, 2, overlay);
            graphics.fill(0, height - 2, width, height, overlay);
            graphics.fill(0, 0, 2, height, overlay);
            graphics.fill(width - 2, 0, width, height, overlay);
        }
    }

    private static String formatTicks(long ticks) {
        long seconds = ticks / 20L;
        return String.format(Locale.ROOT, "%02d:%02d", seconds / 60L, seconds % 60L);
    }
}
