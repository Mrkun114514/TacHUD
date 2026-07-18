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
 *
 * <p><b>ReDeploy integration:</b> system messages (death / respawn) use a
 * non-zero {@code colorOverride} to draw without the × marker, with an explicit
 * colour chosen by the caller. Zero = normal kill entry (isPlayer logic).
 */
public final class KillFeedOverlay {

    private record Entry(String name, boolean isPlayer, long created, int colorOverride) {
        /** Normal kill entry — colour determined by {@link #isPlayer}. */
        Entry(String name, boolean isPlayer, long created) {
            this(name, isPlayer, created, 0);
        }
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

    /**
     * Add a system-level message (e.g. death / respawn) displayed with an
     * explicit colour and no × marker. Used by the ReDeploy integration.
     *
     * @param name  display text, e.g. "☠ 你已阵亡"
     * @param color 0xAARRGGBB
     */
    public static void addDeathMessage(String name, int color) {
        synchronized (ENTRIES) {
            ENTRIES.addFirst(new Entry(name, false, System.currentTimeMillis(), color));
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

                if (e.colorOverride != 0) {
                    // System message (ReDeploy integration): no × marker, explicit colour
                    drawSystemEntry(g, font, e.name(), e.colorOverride, width, y,
                            (int) (kf.marginX * f), alpha);
                } else {
                    int color = e.isPlayer() ? playerColor : mobColor;
                    drawEntry(g, font, e.name(), color, width, y,
                            (int) (kf.marginX * f), alpha);
                }
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

    /**
     * Render a system message entry (from ReDeploy integration) — wider box,
     * no × marker, the entire name text uses the explicit colour.
     */
    private static void drawSystemEntry(GuiGraphics g, Font font, String name, int color,
                                        int width, int y, int marginX, float alpha) {
        int nameW = font.width(name);
        int padX = 8;
        int boxW = padX * 2 + nameW;
        int lineH = font.lineHeight + 2;

        int x2 = width - marginX;
        int x1 = x2 - boxW;
        int y1 = y - 1;
        int y2 = y1 + lineH;

        g.fill(x1, y1, x2, y2, Draw.mulAlpha(0xB0000000, alpha)); // pill background
        // Left accent stripe for system messages (instead of right)
        g.fill(x1, y1, x1 + 2, y2, Draw.mulAlpha(color, alpha));

        int textY = y1 + (lineH - font.lineHeight) / 2 + 1;
        int textX = x2 - marginX + padX; // right-aligned
        g.drawString(font, name, textX, textY, Draw.mulAlpha(color, alpha), true);
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
