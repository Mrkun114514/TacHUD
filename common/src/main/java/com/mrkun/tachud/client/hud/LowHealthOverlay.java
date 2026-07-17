package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * COD-style "critical health" warning: a pulsing red vignette bleeding in from
 * all four screen edges. Intensity scales as health approaches zero, and the
 * whole effect vanishes the instant the player heals above the threshold.
 */
public final class LowHealthOverlay {

    private LowHealthOverlay() {
    }

    public static void render(GuiGraphics g, LocalPlayer player, TacHudConfig cfg,
                              int width, int height, long now) {
        TacHudConfig.LowHealth lh = cfg.lowHealth;
        if (!lh.enabled) return;
        if (player.isSpectator() || player.getAbilities().invulnerable) return;

        float health = player.getHealth();
        double threshold = Math.max(0.5, lh.thresholdHp);
        if (health <= 0f || health > threshold) return;

        // 0 at threshold, ~1 at one heart from death -> deeper red closer to death.
        float intensity = (float) Mth.clamp(1.0 - (health / threshold), 0.08, 1.0);

        // Heartbeat pulse, faster as intensity rises.
        double freq = 0.9 * lh.pulseSpeed * (0.7 + 0.6 * intensity);
        double pulse = 0.5 + 0.5 * Math.sin((now / 1000.0) * Math.PI * 2.0 * freq);
        float alpha = (float) (lh.maxOpacity * intensity * (0.45 + 0.55 * pulse));

        int baseRgb = TacHudConfig.argb(lh.color, 0xFFFF2A25) & 0x00FFFFFF;
        int edge = Draw.withAlpha(baseRgb, alpha);
        int clear = baseRgb; // alpha 0

        int depth = Math.max(6, (int) (Math.min(width, height) * lh.depthFraction));

        // Top and bottom use Vanilla's vertical gradient.
        g.fillGradient(0, 0, width, depth, edge, clear);
        g.fillGradient(0, height - depth, width, height, clear, edge);
        // Left and right use our horizontal gradient helper.
        Draw.gradientH(g, 0, 0, depth, height, edge, clear);
        Draw.gradientH(g, width - depth, 0, width, height, clear, edge);
    }
}
