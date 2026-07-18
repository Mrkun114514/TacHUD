package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.client.hud.KillConfirmOverlay;
import com.mrkun.tachud.client.hud.XpPopOverlay;
import com.mrkun.tachud.client.hud.VanillaHudOverlay;
import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Orchestrates every TacHUD overlay in a fixed draw order. Invoked once per
 * frame from the loader-agnostic {@code ClientGuiEvent.RENDER_HUD} callback.
 *
 * <p><b>ReDeploy integration:</b> when a {@code RedeployDeathScreen} is active
 * (detected via class-name check, zero compile-time dependency) the tactical
 * overlays that would visually clash with the death screen are suppressed.
 * The compass and kill feed remain visible for COD-style situational awareness.
 */
public final class HudRenderer {

    /** Fully-qualified name of {@code RedeployDeathScreen} from the ReDeploy mod. */
    private static final String REDEPLOY_DEATH_SCREEN_CLASS =
            "com.mrkun114514.redeploy.client.RedeployDeathScreen";

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

        // ReDeploy integration: when the COD death screen is open, suppress
        // tactical overlays that would overlap the death UI.
        boolean deathScreen = isRedeployDeathScreen(mc.screen);

        if (!deathScreen) {
            // These overlays would visually clash with the death screen.
            LowHealthOverlay.render(graphics, player, cfg, width, height, now);
            AmmoHudOverlay.render(graphics, mc, player, cfg, width, height);
            HitMarkerOverlay.render(graphics, cfg, width, height, now);
            KillConfirmOverlay.render(graphics, mc, cfg, width, height, now);
            XpPopOverlay.render(graphics, mc, cfg, width, height, now);
        }

        // These remain visible even during the death screen (COD-style).
        CompassOverlay.render(graphics, mc, player, cfg, width, height);
        KillFeedOverlay.render(graphics, mc, cfg, width, height, now);

        // Vanilla‑replacement bars (COD‑style health / armour / food / XP)
        VanillaHudOverlay.render(graphics, mc, cfg, width, height, now);
    }

    // ---- ReDeploy detection (runtime, zero compile dep) ------------------

    /**
     * Returns {@code true} when the current screen is an instance of
     * ReDeploy's {@code RedeployDeathScreen}, detected by class name so that
     * TacHUD never needs a compile-time dependency on the ReDeploy mod.
     */
    private static boolean isRedeployDeathScreen(Screen screen) {
        if (screen == null) return false;
        return REDEPLOY_DEATH_SCREEN_CLASS.equals(screen.getClass().getName());
    }
}
