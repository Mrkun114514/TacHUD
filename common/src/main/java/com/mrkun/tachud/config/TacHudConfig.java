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

    /** Global HUD size multiplier. Combined with an auto screen-height fit
     *  factor (see {@code HudScale}) so the HUD automatically scales to
     *  the interface size. 1.0 = default. */
    public double uiScale = 1.0;

    /** Hit / kill sound effects. */
    public HitSound hitSound = new HitSound();

    /** Kill-confirmation flourish (center-screen "KILL" + streak). */
    public KillConfirm killConfirm = new KillConfirm();

    /** XP gain popup shown just below the crosshair (mainly XP orbs). */
    public XpPop xpPop = new XpPop();

    // ---- Vanilla-HUD replacement (COD-style health/armor/food/XP) --------
    /** Controls for the COD-military-style replacement of vanilla HUD bars. */
    public VanillaHud vanillaHud = new VanillaHud();

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

    // ---- Hit / kill sound effects ----------------------------------------
    public static class HitSound {
        public boolean enabled = true;
        /**
         * Sound played on every landed hit, as a {@code <namespace>:<path>}
         * ResourceLocation. Vanilla sounds work out of the box, e.g.
         * {@code minecraft:block.note_block.hat}. A custom mod sound can be
         * used if it is registered in a {@code sounds.json}.
         */
        public String sound = "minecraft:block.note_block.hat";
        public float volume = 0.7f;
        public float pitch = 1.0f;
        /** Play a distinct sound on the killing blow. */
        public boolean killDistinct = true;
        public String killSound = "minecraft:block.note_block.bass";
    }

    // ---- Kill confirmation (center-screen flourish) --------------------
    public static class KillConfirm {
        public boolean enabled = true;
        /** Relative size multiplier for the flourish. */
        public double size = 1.0;
        /** How long the flourish stays visible, in milliseconds. */
        public int durationMs = 900;
        /** Show the running kill streak (e.g. x3) after the label. */
        public boolean showKillstreak = true;
        /** Milliseconds without a kill before the streak resets to 0. */
        public int streakResetMs = 4000;
        public String color = "#FFFF2A25";
        /** Text shown on kill; empty falls back to "KILL". */
        public String text = "KILL";
    }

    // ---- XP gain popup (below the crosshair) -------------------------
    public static class XpPop {
        public boolean enabled = true;
        /** Relative size multiplier. */
        public double size = 1.0;
        /** How long the "+N XP" stays fully visible, in milliseconds. */
        public int holdMs = 1100;
        /** Fade-out duration, in milliseconds. */
        public int fadeMs = 450;
        public String color = "#FF7CFC00";
        /** Also display the instantaneous gain rate as "XP/s". */
        public boolean showRate = true;
    }

    // ---- Vanilla-HUD replacement (COD-style corner bars) ----------------
    public static class VanillaHud {
        public boolean enabled = false;
        /** Replace health hearts with a compact COD-style bar (bottom-left). */
        public boolean healthEnabled = true;
        /** Replace armor icons with a thin blue bar above the health bar. */
        public boolean armorEnabled = true;
        /** Replace hunger drumsticks with a brown bar (bottom-right). */
        public boolean hungerEnabled = true;
        /** Replace the XP progress bar with a segmented line (experimental). */
        public boolean xpBarEnabled = false;
        /** Replace air bubbles with a blue bar above the armor bar. */
        public boolean airEnabled = true;
        /** Auto-disable hunger/saturation when AppleSkin is detected. */
        public boolean autoHunger = true;

        // ---- Appearance defaults ----------------------------------------
        public double barWidth = 160.0;          // total bar width in pixels
        public double marginBottom = 14.0;       // distance from bottom edge
        public double healthHeight = 8.0;        // health bar thickness
        public double armorHeight = 5.0;         // armor bar thickness
        public double airHeight = 5.0;           // air bar thickness
        public double hungerHeight = 8.0;        // hunger bar thickness
        public double saturationHeight = 5.0;    // saturation bar thickness
        public double segmentGap = 2.0;          // gap between bars
        public int healthSegments = 5;           // number of health segments
        public int armorSegments = 4;            // armor bar segments
        public int hungerSegments = 6;           // hunger bar segments
        public int xpSegments = 5;               // XP bar segments
        public int airSegments = 4;              // air bar segments
        public String healthColor = "#FFCC0000";
        public String healthBgColor = "#FF3A1515";
        public String armorColor = "#FF3CB4FF";
        public String armorBgColor = "#FF1A2A3A";
        public String hungerColor = "#FFC49C48";
        public String hungerBgColor = "#FF3A2A15";
        public String saturationColor = "#FFFFD700"; // golden bar (above hunger)
        public String airColor = "#FF00BFFF";    // sky blue bar (above armor)
        public String airBgColor = "#FF152A3A";  // dark blue background
        public String xpColor = "#FF7CFC00";
        public String xpBgColor = "#FF0A2A0A";
        public String healthExtraColor = "#FFFFD700"; // golden absorption hearts
        public double healthRingRadius = 28.0;    // radius of health ring
        public double healthRingThickness = 6.0;  // thickness of health ring
        public boolean chatEnabled = true;        // enable chat box shift
        public double chatOffsetY = 40.0;         // pixels to shift chat upward

        // ---- Centered tactical layout (concept-art look) ----------------
        /** 居中战术布局：血环/护甲环环绕屏幕中心准星，饱和度在左、饥饿在右、
         *  氧气在下。关闭则使用左下角布局。 */
        public boolean centeredLayout = true;
        /** 居中模式下血环半径（较大以留出中心准星空间）。 */
        public double centeredRingRadius = 46.0;
        /** 渲染发光十字准星（居中布局核心视觉）。 */
        public boolean crosshairEnabled = true;
        /** 准星颜色。 */
        public String crosshairColor = "#FFFFFFFF";
        /** 准星单臂长度（像素）。 */
        public double crosshairSize = 6.0;
        /** 准星中心留空（像素）。 */
        public double crosshairGap = 3.0;
        /** 真实辉光（基于帧缓冲实时模糊 bloom）。关闭则无发光。 */
        public boolean glowEnabled = true;
        /** 辉光强度（0~2，越大越亮）。 */
        public double glowIntensity = 0.8;

        // ---- Position offsets (custom UI positioning) -------------------
        public double healthOffsetX = 0.0;       // X offset for health bar
        public double healthOffsetY = 0.0;       // Y offset for health bar
        public double armorOffsetX = 0.0;        // X offset for armor bar
        public double armorOffsetY = 0.0;        // Y offset for armor bar
        public double airOffsetX = 0.0;          // X offset for air bar
        public double airOffsetY = 0.0;          // Y offset for air bar
        public double hungerOffsetX = 0.0;       // X offset for hunger bar
        public double hungerOffsetY = 0.0;       // Y offset for hunger bar
        public double saturationOffsetX = 0.0;   // X offset for saturation bar
        public double saturationOffsetY = 0.0;   // Y offset for saturation bar

        // ---- Individual bar dimensions ----------------------------------
        public double healthWidth = 160.0;       // health bar width (overrides barWidth)
        public double armorWidth = 160.0;        // armor bar width (overrides barWidth)
        public double airWidth = 160.0;          // air bar width (overrides barWidth)
        public double hungerWidth = 160.0;       // hunger bar width (overrides barWidth)
        public double saturationWidth = 160.0;   // saturation bar width (overrides barWidth)
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
