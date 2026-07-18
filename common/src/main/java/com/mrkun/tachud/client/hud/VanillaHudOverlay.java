package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import com.mrkun.tachud.config.TacHudConfig.VanillaHud;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * COD‑military‑style replacement for the vanilla health / armour / hunger /
 * saturation / XP bars.
 *
 * <p>AppleSkin auto‑detect: when {@code appleskin} is loaded this overlay
 * suppresses the hunger + saturation bars and shows a one‑time chat hint.
 */
public final class VanillaHudOverlay {

    private static boolean appleskinChecked = false;
    private static boolean appleskinLoaded = false;
    private static boolean warningShown = false;

    private VanillaHudOverlay() {
    }

    /**
     * Main entry — draw all custom vanilla‑style bars. Call every frame from
     * {@link HudRenderer} after all TacHUD‑specific overlays.
     */
    public static void render(GuiGraphics g, Minecraft mc, TacHudConfig cfg,
                              int width, int height, long now) {
        VanillaHud vh = cfg.vanillaHud;
        if (!cfg.masterEnabled || !vh.enabled) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        detectAppleSkin();

        double f = HudScale.factor(height, cfg);
        int barW = (int) (vh.barWidth * f);
        int gap = (int) (vh.segmentGap * f);
        int sideMargin = 8;   // offset from screen edge

        int bottom = height - (int) (vh.marginBottom * f);
        int healthH = (int) (vh.healthHeight * f);
        int armorH = vh.armorEnabled && p.getArmorValue() > 0
                ? (int) (vh.armorHeight * f) : 0;

        // ── Left side: health + armour ──────────────────────────────────
        int leftX = sideMargin;
        int healthY = bottom - healthH;
        int armorY = healthY - armorH - gap;

        // Armour bar (above health)
        if (armorH > 0 && vh.armorEnabled) {
            int maxArmor = 20;
            float armorRatio = Math.min(1f, p.getArmorValue() / (float) maxArmor);
            drawSegmentedBar(g, leftX, armorY, barW, armorH, gap,
                    armorRatio, 4, // armour uses 4 segments (5HP each)
                    TacHudConfig.argb(vh.armorColor, 0xFF3CB4FF),
                    TacHudConfig.argb(vh.armorBgColor, 0x55333333));
        }

        // Health bar
        float healthRatio = Math.max(0f, p.getHealth() / p.getMaxHealth());
        int healthClr = TacHudConfig.argb(vh.healthColor, 0xFFCC0000);
        int healthBg = TacHudConfig.argb(vh.healthBgColor, 0x55000000);
        drawSegmentedBar(g, leftX, healthY, barW, healthH, gap,
                healthRatio, vh.healthSegments, healthClr, healthBg);

        // ── Right side: hunger + saturation ────────────────────────────
        if (!appleskinLoaded && vh.autoHunger && vh.hungerEnabled) {
            int rightX = width - barW - sideMargin;
            int hungerH = healthH;
            int foodY = bottom - healthH;

            float hungerRatio = Math.min(1f, p.getFoodData().getFoodLevel() / 20f);
            float satRatio = Math.min(1f, p.getFoodData().getSaturationLevel() / 20f);

            int hColor = TacHudConfig.argb(vh.hungerColor, 0xFFC49C48);
            int hBg = TacHudConfig.argb(vh.hungerBgColor, 0x55222222);

            // Hunger bar
            drawSegmentedBar(g, rightX, foodY, barW, hungerH, gap,
                    hungerRatio, vh.hungerSegments, hColor, hBg);

            // Saturation overlay (golden, drawn on top)
            if (satRatio > 0.01f) {
                int satClr = TacHudConfig.argb(vh.saturationColor, 0x55FFD700);
                drawSegmentedBar(g, rightX, foodY, barW, hungerH, gap,
                        satRatio, vh.hungerSegments, satClr, 0x00000000);
            }
        }

        // ── XP bar (centered, below hotbar area) ───────────────────────
        if (vh.xpBarEnabled && p.totalExperience > 0) {
            int xpW = (int) (vh.barWidth * f * 1.2);
            int xpH = (int) (vh.healthHeight * f * 0.8);
            int xpBottom = healthY - armorH - (int) (12 * f);
            int xpX = width / 2 - xpW / 2;

            int xpFull = p.getXpNeededForNextLevel();
            float xpRatio = xpFull > 0
                    ? Math.min(1f, p.totalExperience / (float) xpFull)
                    : p.experienceProgress;

            int xpClr = TacHudConfig.argb(vh.xpColor, 0xFF00FF00);
            int xpBg = TacHudConfig.argb(vh.xpBgColor, 0x3300AA00);
            drawSegmentedBar(g, xpX, xpBottom, xpW, xpH, (int) (gap * 0.8),
                    xpRatio, vh.xpSegments, xpClr, xpBg);
        }
    }

    // ── AppleSkin detection ────────────────────────────────────────────

    private static void detectAppleSkin() {
        if (appleskinChecked) return;
        appleskinChecked = true;
        try {
            appleskinLoaded = Platform.isModLoaded("appleskin");
        } catch (Throwable ignored) {
            appleskinLoaded = false;
        }
        if (appleskinLoaded) {
            TacHudConfig cfg = com.mrkun.tachud.config.ConfigManager.get();
            cfg.vanillaHud.autoHunger = false;
        }
    }

    /** Call once after joining a world to show the AppleSkin hint. */
    public static void showAppleSkinWarning(Minecraft mc) {
        if (!appleskinLoaded || warningShown) return;
        warningShown = true;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal(
                            "§e[TacHUD] §f检测到 AppleSkin → "
                                    + "已自动关闭饥饿/饱和度美化，避免冲突"), true);
        }
    }

    // ── Segmented‑bar drawing ──────────────────────────────────────────

    /**
     * Draw a horizontal bar composed of segments, each separated by a small
     * gap. The filled proportion is visualised by <i>whole segments</i>
     * (a fractional segment is drawn at the end).
     */
    private static void drawSegmentedBar(GuiGraphics g,
                                         int x, int y, int totalW, int h, int gap,
                                         float ratio, int segments,
                                         int fillColor, int bgColor) {
        double segW = (totalW - (segments - 1) * gap) / (double) segments;
        int segWI = (int) Math.round(segW);
        int filledN = (int) Math.floor(ratio * segments);

        // Background (all segments, dark)
        for (int i = 0; i < segments; i++) {
            int sx = x + i * (segWI + gap);
            if (bgColor != 0) {
                g.fill(sx, y, sx + segWI, y + h, bgColor);
            }
        }

        // Filled segments
        for (int i = 0; i < filledN; i++) {
            int sx = x + i * (segWI + gap);
            g.fill(sx, y, sx + segWI, y + h, fillColor);
        }

        // Partial segment (the fractional part)
        float remainder = ratio * segments - filledN;
        if (remainder > 0.001f && filledN < segments) {
            int sx = x + filledN * (segWI + gap);
            int partialW = Math.max(1, (int) Math.round(segWI * remainder));
            g.fill(sx, y, sx + partialW, y + h, fillColor);
        }
    }
}
