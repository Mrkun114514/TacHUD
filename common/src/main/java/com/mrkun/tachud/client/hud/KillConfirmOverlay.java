package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Center-screen kill confirmation, echoing the COD "eliminated" flourish: a
 * quick red "KILL" pop plus an expanding target-lock frame, optionally showing
 * the running kill streak. Triggered from the server-authoritative kill packet.
 */
public final class KillConfirmOverlay {

    private static volatile long lastKill = Long.MIN_VALUE;
    private static volatile long lastStreakKill = Long.MIN_VALUE;
    private static volatile int streak = 0;

    private KillConfirmOverlay() {
    }

    public static void trigger() {
        TacHudConfig cfg = ConfigManager.get();
        long now = System.currentTimeMillis();
        if (now - lastStreakKill > cfg.killConfirm.streakResetMs) {
            streak = 0;
        }
        streak++;
        lastStreakKill = now;
        lastKill = now;
    }

    public static void render(GuiGraphics g, Minecraft mc, TacHudConfig cfg,
                              int width, int height, long now) {
        TacHudConfig.KillConfirm kc = cfg.killConfirm;
        if (!kc.enabled) return;

        long age = now - lastKill;
        if (age < 0 || age > kc.durationMs) return;

        double f = HudScale.factor(height, cfg);
        float t = (float) age / Math.max(1, kc.durationMs);
        float fadeIn = Mth.clamp(age / 80f, 0f, 1f);
        float alpha = (1f - t) * fadeIn;
        int baseRgb = TacHudConfig.argb(kc.color, 0xFFFF2A25) & 0x00FFFFFF;
        int color = Draw.withAlpha(baseRgb, alpha);

        int cx = width / 2;
        int cy = height / 2;

        // Expanding target-lock frame (screen-space, scaled by fit factor).
        int half = Math.round((float) (54.0 * (0.6 + 0.4 * (1.0 - t)) * f));
        int th = Math.max(2, Math.round((float) (3.0 * f)));
        int len = Math.max(8, Math.round((float) (14.0 * f)));
        int c = color;
        // top-left, top-right, bottom-left, bottom-right brackets
        bracket(g, cx - half, cy - half, +1, +1, len, th, c);
        bracket(g, cx + half, cy - half, -1, +1, len, th, c);
        bracket(g, cx - half, cy + half, +1, -1, len, th, c);
        bracket(g, cx + half, cy + half, -1, -1, len, th, c);

        // Centered "KILL" text, scaled around the crosshair via the pose matrix.
        Font font = mc.font;
        String txt = (kc.text != null && !kc.text.isBlank()) ? kc.text : "KILL";
        float textScale = (float) (kc.size * f * (0.9 + 0.3 * (1.0 - t)));
        var pose = g.pose();
        pose.pushMatrix();
        pose.translate(cx, cy - 2f);
        pose.scale(textScale, textScale);
        g.drawCenteredString(font, txt, 0, 0, color);
        pose.popMatrix();

        // Kill streak, just below the label.
        if (kc.showKillstreak && streak > 1) {
            String sx = "x" + streak;
            int sy = cy + Math.round((float) (20f * f));
            int streakColor = Draw.withAlpha(0xFFFFC400 & 0x00FFFFFF, alpha);
            pose.pushMatrix();
            pose.translate(cx, sy);
            pose.scale((float) (kc.size * f), (float) (kc.size * f));
            g.drawCenteredString(font, sx, 0, 0, streakColor);
            pose.popMatrix();
        }
    }

    private static void bracket(GuiGraphics g, int x, int y, int dx, int dy,
                                int len, int th, int color) {
        // horizontal arm
        g.fill(x, y, x + dx * len, y + th, color);
        // vertical arm
        g.fill(x, y, x + th, y + dy * len, color);
    }
}
