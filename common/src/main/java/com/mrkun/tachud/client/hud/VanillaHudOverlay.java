package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import com.mrkun.tachud.config.TacHudConfig.VanillaHud;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * COD-military-style replacement for the vanilla health / armour / hunger /
 * saturation bars.
 *
 * <p>Layout (COD-style corner placement):
 * <ul>
 *   <li>Bottom-left:  armour bar (thin, above) + health bar (below)</li>
 *   <li>Bottom-right: saturation bar (golden, above) + hunger bar (below)</li>
 *   <li>XP bar (optional, disabled by default): thin segmented line at the
 *       very bottom edge of the screen, full hotbar width.</li>
 * </ul>
 *
 * <p>All bars stay in the screen corners — they never extend into the chat
 * area (bottom-left) or the hotbar area (bottom-center).
 *
 * <p>AppleSkin auto-detect: when {@code appleskin} is loaded the hunger +
 * saturation bars are suppressed and a one-time chat hint is shown.
 */
public final class VanillaHudOverlay {

    private static boolean appleskinChecked = false;
    private static boolean appleskinLoaded = false;
    private static boolean warningShown = false;

    private VanillaHudOverlay() {
    }

    /**
     * Main entry — draw all custom COD-style bars. Call every frame from
     * {@link HudRenderer} after all TacHUD-specific overlays.
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
        int sideMargin = Math.max(2, (int) (6 * f));
        int healthH = Math.max(2, (int) (vh.healthHeight * f));
        int armorH = Math.max(2, (int) (vh.armorHeight * f));
        int gap = Math.max(1, (int) (2 * f));
        int bottomMargin = Math.max(2, (int) (vh.marginBottom * f));
        int textOff = Math.max(3, (int) (4 * f));

        // ── Y positions ────────────────────────────────────────────────
        // Health at the very bottom; armour directly above it (COD style).
        int healthY = height - bottomMargin - healthH;
        int armorY  = healthY - gap - armorH;
        // Right side mirrors left.
        int hungerY = healthY;
        int satY    = armorY;

        // ── Left side: armour (above) + health (below) ─────────────────
        int leftX = sideMargin;

        if (vh.armorEnabled && p.getArmorValue() > 0) {
            float armorRatio = Math.min(1f, p.getArmorValue() / 20f);
            int armorClr  = TacHudConfig.argb(vh.armorColor, 0xFF3CB4FF);
            int armorBg   = TacHudConfig.argb(vh.armorBgColor, 0xFF1A2A3A);
            drawTacticalBar(g, leftX, armorY, barW, armorH,
                    armorRatio, 4, armorClr, armorBg);
            g.drawString(mc.font, String.valueOf((int) p.getArmorValue()),
                    leftX + barW + textOff, armorY - 1, armorClr, false);
        }

        if (vh.healthEnabled) {
            float healthRatio = Math.max(0f, p.getHealth() / p.getMaxHealth());
            int hpClr  = TacHudConfig.argb(vh.healthColor, 0xFFCC0000);
            int hpBg   = TacHudConfig.argb(vh.healthBgColor, 0xFF3A1515);
            drawTacticalBar(g, leftX, healthY, barW, healthH,
                    healthRatio, vh.healthSegments, hpClr, hpBg);
            String hpText = (int) p.getHealth() + "/" + (int) p.getMaxHealth();
            g.drawString(mc.font, hpText,
                    leftX + barW + textOff, healthY - 1, hpClr, false);
        }

        // ── Right side: saturation (above) + hunger (below) ────────────
        if (!appleskinLoaded && vh.autoHunger && vh.hungerEnabled) {
            int rightX = width - sideMargin - barW;

            // Saturation bar (golden, above hunger)
            float satRatio = Math.min(1f,
                    p.getFoodData().getSaturationLevel() / 20f);
            if (satRatio > 0.01f) {
                int satClr = TacHudConfig.argb(vh.saturationColor, 0xFFFFD700);
                int satBg  = TacHudConfig.argb(vh.hungerBgColor, 0xFF3A3015);
                drawTacticalBar(g, rightX, satY, barW, armorH,
                        satRatio, vh.hungerSegments, satClr, satBg);
            }

            // Hunger bar (below)
            float hungerRatio = Math.min(1f,
                    p.getFoodData().getFoodLevel() / 20f);
            int hClr = TacHudConfig.argb(vh.hungerColor, 0xFFC49C48);
            int hBg  = TacHudConfig.argb(vh.hungerBgColor, 0xFF3A2A15);
            drawTacticalBar(g, rightX, hungerY, barW, healthH,
                    hungerRatio, vh.hungerSegments, hClr, hBg);
            String foodText = String.valueOf(p.getFoodData().getFoodLevel());
            g.drawString(mc.font, foodText,
                    rightX - textOff - mc.font.width(foodText),
                    hungerY - 1, hClr, false);
        }

        // ── XP bar (optional, at very bottom edge) ─────────────────────
        if (vh.xpBarEnabled) {
            int hotbarW = (int) (182 * f);
            int xpW = hotbarW;
            int xpX = width / 2 - xpW / 2;
            int xpH = Math.max(2, (int) (3 * f));
            int xpY = height - xpH;

            float xpRatio = Math.min(1f, Math.max(0f, p.experienceProgress));
            int xpClr = TacHudConfig.argb(vh.xpColor, 0xFF7CFC00);
            int xpBg  = TacHudConfig.argb(vh.xpBgColor, 0xFF0A2A0A);
            drawTacticalBar(g, xpX, xpY, xpW, xpH,
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
    }

    /** Call once after joining a world to show the AppleSkin hint. */
    public static void showAppleSkinWarning(Minecraft mc) {
        if (!appleskinLoaded || warningShown) return;
        warningShown = true;
        if (mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal(
                            "\u00a7e[TacHUD] \u00a7f\u68c0\u6d4b\u5230 AppleSkin \u2192 "
                                    + "\u5df2\u81ea\u52a8\u5173\u95ed\u9965\u997f/\u9971\u548c\u5ea6\u7f8e\u5316\uff0c\u907f\u514d\u51b2\u7a81"),
                    true);
        }
    }

    // ── Tactical bar drawing ───────────────────────────────────────────

    /**
     * Draw a continuous horizontal bar with segment dividers and a 1px border
     * frame. Much cleaner than the old separate-segment approach.
     *
     * @param x         top-left X
     * @param y         top-left Y
     * @param w         total width
     * @param h         bar height
     * @param ratio     fill ratio (0.0–1.0)
     * @param segments  number of segment dividers to draw
     * @param fillColor ARGB fill colour
     * @param bgColor   ARGB background colour
     */
    private static void drawTacticalBar(GuiGraphics g,
                                        int x, int y, int w, int h,
                                        float ratio, int segments,
                                        int fillColor, int bgColor) {
        // Background (full bar)
        g.fill(x, y, x + w, y + h, bgColor);

        // Fill (proportional)
        int fillW = Math.max(0,
                (int) Math.round(w * Math.min(1f, Math.max(0f, ratio))));
        if (fillW > 0) {
            g.fill(x, y, x + fillW, y + h, fillColor);
        }

        // Segment dividers (thin dark vertical lines)
        if (segments > 1 && h >= 3) {
            int segW = w / segments;
            for (int i = 1; i < segments; i++) {
                int dx = x + i * segW;
                g.fill(dx, y, dx + 1, y + h, 0x33000000);
            }
        }

        // Border frame (1px, semi-transparent dark)
        if (h >= 3) {
            g.fill(x, y, x + w, y + 1, 0x66000000);           // top
            g.fill(x, y + h - 1, x + w, y + h, 0x66000000);   // bottom
        }
        g.fill(x, y, x + 1, y + h, 0x66000000);               // left
        g.fill(x + w - 1, y, x + w, y + h, 0x66000000);       // right
    }
}
