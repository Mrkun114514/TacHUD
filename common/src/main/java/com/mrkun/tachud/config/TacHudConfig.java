package com.mrkun.tachud.config;

/**
 * Plain data holder for every user-tunable option, serialized to
 * {@code config/tachud.json} via Gson. Every field has a sane military-HUD
 * default so a fresh install works out of the box.
 *
 * <p>Colors are stored as ARGB hex strings (e.g. {@code "#FFFF3B30"}) so they
 * are easy to hand-edit; {@link #argb(String, int)} parses them safely.
 */
public class TacHudConfig {

    /** Master switch. When false, nothing renders. */
    public boolean masterEnabled = true;

    public LowHealth lowHealth = new LowHealth();
    public KillFeed killFeed = new KillFeed();
    public AmmoHud ammoHud = new AmmoHud();
    public Compass compass = new Compass();
    public HitMarker hitMarker = new HitMarker();

    // ---- Low-health warning (red screen-edge vignette) ---------------------
    public static class LowHealth {
        public boolean enabled = true;
        /** Trigger when current health (in half-hearts / HP) is at or below this. Default 8 HP = 4 hearts. */
        public double thresholdHp = 8.0;
        /** Vignette depth as a fraction of the smaller screen dimension. */
        public double depthFraction = 0.18;
        /** Pulse speed multiplier (higher = faster flashing). */
        public double pulseSpeed = 1.0;
        /** Max opacity of the vignette at the brink of death (0-1). */
        public double maxOpacity = 0.85;
        public String color = "#FFFF2A25";
    }

    // ---- Kill feed (top-right pop-ups) -------------------------------------
    public static class KillFeed {
        public boolean enabled = true;
        /** Anchor from the right edge, in scaled pixels. */
        public int marginX = 6;
        /** Anchor from the top edge, in scaled pixels. */
        public int marginY = 6;
        /** Also show kills of non-player entities (mobs, animals). */
        public boolean showMobKills = true;
        public int maxEntries = 4;
        /** How long an entry stays fully visible, in milliseconds. */
        public int holdMs = 2600;
        /** Fade-in / fade-out duration, in milliseconds. */
        public int fadeMs = 320;
        public String playerColor = "#FFFF4D4D";
        public String mobColor = "#FFF0F0F0";
        public String labelColor = "#FFB8B8B8";
    }

    // ---- Ammo / durability counter (bottom-left) --------------------------
    public static class AmmoHud {
        public boolean enabled = true;
        /** Offset from the left edge. */
        public int marginX = 8;
        /** Offset from the bottom edge. */
        public int marginY = 8;
        /** Show numeric durability percentage for tools/weapons. */
        public boolean showDurabilityPercent = true;
        /** Also read the off-hand item when the main hand has nothing to show. */
        public boolean useOffhandFallback = true;
        public String accentColor = "#FFFFC400";
        public String lowColor = "#FFFF3B30";
        public String textColor = "#FFFFFFFF";
    }

    // ---- Tactical compass (top-center) ------------------------------------
    public static class Compass {
        public boolean enabled = true;
        /** Distance from the top edge. */
        public int marginY = 4;
        /** Total width of the compass strip. */
        public int width = 180;
        /** Field of view of the strip, in degrees. */
        public double fov = 120.0;
        public String color = "#FFFFFFFF";
        public String accentColor = "#FFFFC400";
    }

    // ---- Hitmarker (crosshair X flash) ------------------------------------
    public static class HitMarker {
        public boolean enabled = true;
        public double size = 5.0;
        public double gap = 2.0;
        public int durationMs = 260;
        public String color = "#FFFFFFFF";
        public String killColor = "#FFFF2A25";
    }

    /** Parse an ARGB hex string like {@code #FFFF2A25}; falls back on error. */
    public static int argb(String hex, int fallback) {
        if (hex == null) return fallback;
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);
        try {
            long v = Long.parseLong(s, 16);
            if (s.length() <= 6) {
                // No alpha supplied -> assume fully opaque.
                v |= 0xFF000000L;
            }
            return (int) v;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
