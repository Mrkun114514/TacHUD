package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Lightweight, loader-agnostic XP-gain popup shown just below the crosshair.
 *
 * <p>It samples the local player's cumulative {@code totalExperience} every frame
 * and, whenever it rises, pops a fading "+N XP" (the dominant source in
 * survival play is absorbing XP orbs). A short burst window also derives an
 * instantaneous "XP/s" rate. No platform hooks or network needed, so the exact
 * same code runs on Fabric and NeoForge.
 */
public final class XpPopOverlay {

    private static volatile int lastXp = -1;
    private static volatile long lastGainTime = Long.MIN_VALUE;
    private static volatile long burstStart = Long.MIN_VALUE;
    private static volatile int pending = 0;

    private XpPopOverlay() {
    }

    public static void render(GuiGraphics g, Minecraft mc, TacHudConfig cfg,
                              int width, int height, long now) {
        TacHudConfig.XpPop xp = cfg.xpPop;
        if (!xp.enabled) return;

        var player = mc.player;
        if (player == null) return;

        int cur = player.totalExperience;
        if (lastXp < 0) {
            lastXp = cur;
            return;
        }
        if (cur > lastXp) {
            int gain = cur - lastXp;
            if (burstStart == Long.MIN_VALUE || now - lastGainTime > xp.holdMs + xp.fadeMs) {
                burstStart = now;
                pending = 0;
            }
            pending += gain;
            lastGainTime = now;
        }
        lastXp = cur;

        long age = now - lastGainTime;
        long life = (long) xp.holdMs + xp.fadeMs;
        if (age < 0 || age > life || pending <= 0) {
            if (age > life) {
                pending = 0;
                burstStart = Long.MIN_VALUE;
            }
            return;
        }

        double f = HudScale.factor(height, cfg);
        float alpha;
        if (age < xp.fadeMs) {
            alpha = (float) age / Math.max(1, xp.fadeMs);          // fade in
        } else if (age < xp.fadeMs + xp.holdMs) {
            alpha = 1f;                                              // hold
        } else {
            alpha = 1f - (float) (age - xp.fadeMs - xp.holdMs) / Math.max(1, xp.fadeMs);
        }
        int baseRgb = TacHudConfig.argb(xp.color, 0xFF7CFC00) & 0x00FFFFFF;
        int color = Draw.withAlpha(baseRgb, alpha);

        int cx = width / 2;
        int cy = height / 2;
        Font font = mc.font;

        String main = "+" + pending + " XP";
        float mainScale = (float) (xp.size * f);

        var pose = g.pose();
        float yMain = cy + (float) (24.0 * f);
        pose.pushMatrix();
        pose.translate(cx, yMain);
        pose.scale(mainScale, mainScale);
        g.drawCenteredString(font, main, 0, 0, color);
        pose.popMatrix();

        if (xp.showRate && burstStart != Long.MIN_VALUE) {
            double secs = Math.max(0.4, (now - burstStart) / 1000.0);
            double rate = pending / secs;
            String rateStr = String.format("%.1f XP/s", rate);
            float rateScale = (float) (xp.size * f * 0.8);
            float yRate = cy + (float) (24.0 * f) + (float) (12.0 * f);
            pose.pushMatrix();
            pose.translate(cx, yRate);
            pose.scale(rateScale, rateScale);
            g.drawCenteredString(font, rateStr, 0, 0, Draw.withAlpha(0xFFB8B8B8 & 0x00FFFFFF, alpha));
            pose.popMatrix();
        }
    }

    /** Drop cached state, e.g. on disconnect, so a new world starts clean. */
    public static void reset() {
        lastXp = -1;
        lastGainTime = Long.MIN_VALUE;
        burstStart = Long.MIN_VALUE;
        pending = 0;
    }
}
