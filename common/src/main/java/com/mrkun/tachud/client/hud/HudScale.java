package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.util.Mth;

/**
 * Single source of truth for how much every overlay should be scaled.
 *
 * <p>The factor combines the user's manual {@code uiScale} multiplier with an
 * automatic fit derived from the screen height, so the HUD "贴合" (fits) the
 * interface size: on a 1080p screen at {@code uiScale=1} the factor is 1.0;
 * on a 4K screen it grows, on a small laptop it shrinks. Hard-clamped so a
 * weird window size can never blow the HUD up to unreadable proportions.
 */
public final class HudScale {

    /** Reference height the base scale is tuned for. */
    private static final double REFERENCE_HEIGHT = 1080.0;
    private static final double MIN = 0.5;
    private static final double MAX = 3.0;

    private HudScale() {
    }

    public static double factor(int height, TacHudConfig cfg) {
        double autoFit = height / REFERENCE_HEIGHT;
        return Mth.clamp(cfg.uiScale * autoFit, MIN, MAX);
    }
}
