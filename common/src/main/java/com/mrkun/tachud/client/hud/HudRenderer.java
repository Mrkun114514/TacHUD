package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

/**
 * Orchestrates every TacHUD overlay in a fixed draw order. Invoked once per
 * frame from the loader-agnostic {@code ClientGuiEvent.RENDER_HUD} callback.
 */
public final class HudRenderer {

    private HudRenderer() {
    }

    public static void render(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        TacHudConfig cfg = ConfigManager.get();

        if (!cfg.masterEnabled) return;
        if (mc.options.hideGui) return;

        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        long now = System.currentTimeMillis();

        // Order matters: full-screen vignette first (behind), markers last (front).
        LowHealthOverlay.render(graphics, player, cfg, width, height, now);
        AmmoHudOverlay.render(graphics, mc, player, cfg, width, height);
        CompassOverlay.render(graphics, mc, player, cfg, width, height);
        KillFeedOverlay.render(graphics, mc, cfg, width, height, now);
        HitMarkerOverlay.render(graphics, cfg, width, height, now);
    }
}
