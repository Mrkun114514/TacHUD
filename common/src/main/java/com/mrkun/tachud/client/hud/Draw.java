package com.mrkun.tachud.client.hud;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/**
 * Small drawing helpers shared by all overlays: alpha manipulation, color
 * interpolation and a horizontal gradient (Vanilla's {@code fillGradient} only
 * does vertical ones).
 */
public final class Draw {

    private Draw() {
    }

    /** Multiply an ARGB color's alpha channel by {@code factor} (0-1). */
    public static int mulAlpha(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        a = Mth.clamp(Math.round(a * factor), 0, 255);
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    /** Replace an ARGB color's alpha with an absolute value (0-1). */
    public static int withAlpha(int rgb, float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255f), 0, 255);
        return (a << 24) | (rgb & 0x00FFFFFF);
    }

    /** Per-channel linear interpolation between two ARGB colors. */
    public static int lerpColor(int from, int to, float t) {
        t = Mth.clamp(t, 0f, 1f);
        int fa = (from >>> 24) & 0xFF, fr = (from >> 16) & 0xFF, fg = (from >> 8) & 0xFF, fb = from & 0xFF;
        int ta = (to >>> 24) & 0xFF, tr = (to >> 16) & 0xFF, tg = (to >> 8) & 0xFF, tb = to & 0xFF;
        int a = Math.round(Mth.lerp(t, fa, ta));
        int r = Math.round(Mth.lerp(t, fr, tr));
        int g = Math.round(Mth.lerp(t, fg, tg));
        int b = Math.round(Mth.lerp(t, fb, tb));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Horizontal gradient rectangle from {@code colorLeft} to {@code colorRight}. */
    public static void gradientH(GuiGraphics g, int x1, int y1, int x2, int y2, int colorLeft, int colorRight) {
        int width = x2 - x1;
        if (width <= 0) return;
        for (int i = 0; i < width; i++) {
            float t = (float) i / width;
            g.fill(x1 + i, y1, x1 + i + 1, y2, lerpColor(colorLeft, colorRight, t));
        }
    }
}
