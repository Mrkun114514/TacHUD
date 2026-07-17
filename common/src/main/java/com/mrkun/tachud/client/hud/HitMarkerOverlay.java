package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.client.hud.HudScale;
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

        double f = HudScale.factor(height, cfg);
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
        int gap = Math.round((float) ((hm.gap + t * hm.size * 0.6) * f));
        int len = Math.max(2, Math.round((float) (hm.size * f)));

        for (float angle : new float[]{45f, 135f, 225f, 315f}) {
            prong(g, cx, cy, angle, gap, len, color, f);
        }
    }

    /**
     * Draws one prong as a short thick line starting at radius {@code gap} from the
     * crosshair and extending {@code len} pixels along {@code angleDeg}.
     *
     * <p>1.21.8 removed {@code PoseStack} (and its {@code pushPose}/{@code popPose}/
     * {@code mulPose} methods); {@code GuiGraphics.pose()} now returns a
     * {@code Matrix3x2fStack}. Rather than depend on that matrix API, we
     * rasterize the rotated bar directly with {@link GuiGraphics#fill}.
     */
    private static void prong(GuiGraphics g, float cx, float cy, float angleDeg,
                              int gap, int len, int color, double f) {
        double rad = Math.toRadians(angleDeg);
        float dx = (float) Math.cos(rad);
        float dy = (float) Math.sin(rad);
        int th = Math.max(1, Math.round((float) (2.0 * f))); // prong thickness in pixels
        int half = th / 2;
        for (int i = 0; i <= len; i++) {
            float d = gap + i;
            int px = Math.round(cx + dx * d);
            int py = Math.round(cy + dy * d);
            g.fill(px - half, py - half, px - half + th, py - half + th, color);
        }
    }
}
