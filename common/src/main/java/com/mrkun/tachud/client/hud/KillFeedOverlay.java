package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.client.hud.HudScale;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Top-right kill feed. Each confirmed kill (delivered from the server) pops in,
 * holds, then fades out - echoing MW's "+100" eliminations, minus the score.
 * Player kills and mob kills are color-coded.
 */
public final class KillFeedOverlay {

    private record Entry(String name, boolean isPlayer, long created) {
    }

    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private KillFeedOverlay() {
    }

    public static void addKill(String name, boolean isPlayer) {
        synchronized (ENTRIES) {
            ENTRIES.addFirst(new Entry(name, isPlayer, System.currentTimeMillis()));
            while (ENTRIES.size() > 12) {
                ENTRIES.removeLast();
            }
        }
    }

    public static void render(GuiGraphics g, Minecraft mc, TacHudConfig cfg,
                              int width, int height, long now) {
        TacHudConfig.KillFeed kf = cfg.killFeed;
        if (!kf.enabled) return;

        double f = HudScale.factor(height, cfg);
        long life = kf.holdMs + 2L * kf.fadeMs;
        Font font = mc.font;
        int playerColor = TacHudConfig.argb(kf.playerColor, 0xFFFF4D4D);
        int mobColor = TacHudConfig.argb(kf.mobColor, 0xFFF0F0F0);

        synchronized (ENTRIES) {
            ENTRIES.removeIf(e -> now - e.created > life);

            int y = (int) (kf.marginY * f);
            int shown = 0;
            for (Entry e : ENTRIES) {
                if (shown >= kf.maxEntries) break;
                float alpha = fadeAlpha(now - e.created, kf.holdMs, kf.fadeMs);
                if (alpha <= 0.02f) continue;

                int color = e.isPlayer() ? playerColor : mobColor;
                drawEntry(g, font, e.name(), color, width, y, (int) (kf.marginX * f), alpha);
                y += font.lineHeight + 5;
                shown++;
            }
        }
    }

    private static void drawEntry(GuiGraphics g, Font font, String name, int color,
                                  int width, int y, int marginX, float alpha) {
        String marker = "\u00D7"; // multiplication sign, always present in the font
        int markerW = font.width(marker);
        int nameW = font.width(name);
        int padX = 4;
        int boxW = padX + markerW + 4 + nameW + padX;
        int lineH = font.lineHeight + 2;

        int x2 = width - marginX;
        int x1 = x2 - boxW;
        int y1 = y - 1;
        int y2 = y1 + lineH;

        g.fill(x1, y1, x2, y2, Draw.mulAlpha(0xB0000000, alpha)); // pill background
        g.fill(x2 - 2, y1, x2, y2, Draw.mulAlpha(color, alpha));  // right accent

        int textY = y1 + (lineH - font.lineHeight) / 2 + 1;
        int markerX = x1 + padX;
        int nameX = markerX + markerW + 4;
        g.drawString(font, marker, markerX, textY, Draw.mulAlpha(color, alpha), true);
        g.drawString(font, name, nameX, textY, Draw.mulAlpha(0xFFFFFFFF, alpha), true);
    }

    private static float fadeAlpha(long age, int holdMs, int fadeMs) {
        if (age < 0) return 0f;
        if (age < fadeMs) return (float) age / fadeMs;              // fade in
        if (age < fadeMs + holdMs) return 1f;                       // hold
        if (age < fadeMs + holdMs + fadeMs) {                       // fade out
            return 1f - (float) (age - fadeMs - holdMs) / fadeMs;
        }
        return 0f;
    }

    /** Optional: called on disconnect to avoid stale entries. */
    public static void clear() {
        synchronized (ENTRIES) {
            ENTRIES.clear();
        }
    }
}
