package com.mrkun.tachud.client.hud;

import com.mojang.math.Axis;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.gui.GuiGraphics;

/**
 * The iconic COD hitmarker: a four-pronged "X" that snaps in at the crosshair
 * on every landed hit, expanding slightly and fading out. The killing blow
 * flashes in a distinct color.
 */
public final class HitMarkerOverlay {

    private static volatile long lastHit = Long.MIN_VALUE;
    private static volatile boolean lastFatal = false;

    private HitMarkerOverlay() {
    }

    public static void trigger(boolean fatal) {
        lastHit = System.currentTimeMillis();
        lastFatal = fatal;
    }

    public static void render(GuiGraphics g, TacHudConfig cfg, int width, int height, long now) {
        TacHudConfig.HitMarker hm = cfg.hitMarker;
        if (!hm.enabled) return;

        long age = now - lastHit;
        if (age < 0 || age > hm.durationMs) return;

        float t = (float) age / Math.max(1, hm.durationMs);
        float alpha = 1f - t;
        int baseRgb = (lastFatal
                ? TacHudConfig.argb(hm.killColor, 0xFFFF2A25)
                : TacHudConfig.argb(hm.color, 0xFFFFFFFF)) & 0x00FFFFFF;
        int color = Draw.withAlpha(baseRgb, alpha * 0.95f);

        float cx = width / 2f;
        float cy = height / 2f;
        int gap = Math.round((float) hm.gap + t * (float) hm.size * 0.6f);
        int len = Math.max(2, Math.round((float) hm.size));

        for (float angle : new float[]{45f, 135f, 225f, 315f}) {
            prong(g, cx, cy, angle, gap, len, color);
        }
    }

    private static void prong(GuiGraphics g, float cx, float cy, float angleDeg,
                              int gap, int len, int color) {
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().mulPose(Axis.ZP.rotationDegrees(angleDeg));
        g.fill(gap, -1, gap + len, 1, color);
        g.pose().popPose();
    }
}
