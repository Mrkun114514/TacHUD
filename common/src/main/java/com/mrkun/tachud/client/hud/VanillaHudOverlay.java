package com.mrkun.tachud.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mrkun.tachud.config.TacHudConfig;
import com.mrkun.tachud.config.TacHudConfig.VanillaHud;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * 状态值美化覆盖层：同心环形血条/护甲 + 进度条式饥饿/氧气。
 *
 * <p>布局：
 * <ul>
 *   <li>左下角：同心环 — 内环血量(红)、中环吸收血量(金)、外环护甲(10段蓝)</li>
 *   <li>血环右侧：饥饿/饱和度进度条（垂直堆叠）</li>
 *   <li>氧气进度条在血环上方（仅水下显示）</li>
 *   <li>经验条：使用原版（不加自制覆盖层）</li>
 * </ul>
 *
 * <p>发光效果：使用 RenderSystem 加法混合（SRC_ALPHA, ONE），
 * 让半透明层叠加到场景上产生真实的光晕扩散，而非贴半透明色块。
 */
public final class VanillaHudOverlay {

    private static boolean appleskinChecked = false;
    private static boolean appleskinLoaded = false;
    private static boolean warningShown = false;

    private VanillaHudOverlay() {
    }

    public static void render(GuiGraphics g, Minecraft mc, TacHudConfig cfg,
                              int width, int height, long now) {
        VanillaHud vh = cfg.vanillaHud;
        if (!cfg.masterEnabled || !vh.enabled) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        detectAppleSkin();

        double f = HudScale.factor(height, cfg);
        int sideMargin = Math.max(2, (int) (6 * f));
        int bottomMargin = Math.max(2, (int) (vh.marginBottom * f));

        int radius = Math.max(6, (int) (vh.healthRingRadius * f));
        int thickness = Math.max(2, (int) (vh.healthRingThickness * f));

        int cx = sideMargin + radius + (int) (vh.healthOffsetX * f);
        int cy = height - bottomMargin - radius + (int) (vh.healthOffsetY * f);

        if (vh.healthEnabled) {
            drawConcentricRings(g, mc, p, vh, f, cx, cy, radius, thickness);
        }

        boolean showAir = vh.airEnabled && p.getAirSupply() < p.getMaxAirSupply();
        boolean showHunger = !appleskinLoaded && vh.autoHunger && vh.hungerEnabled;

        if (showAir) {
            drawAirFan(g, mc, p, vh, f, width, height);
        }

        if (showHunger) {
            drawSideBars(g, mc, p, vh, f, cx, cy, radius, thickness);
        }
    }

    // ── 同心环绘制（加法混合发光）────────────────────────────────────

    private static void drawConcentricRings(GuiGraphics g, Minecraft mc,
                                            LocalPlayer p, VanillaHud vh,
                                            double f, int cx, int cy,
                                            int radius, int thickness) {
        float health = p.getHealth();
        float maxHealth = p.getMaxHealth();
        float absorption = p.getAbsorptionAmount();
        float armor = p.getArmorValue();

        int gap = Math.max(1, (int) (2 * f));

        // ── 内环：血量（红色，加法混合发光）──
        int healthBg = TacHudConfig.argb(vh.healthBgColor, 0xFF3A1515);
        int healthClr = TacHudConfig.argb(vh.healthColor, 0xFFCC0000);
        float healthRatio = Math.min(1f, health / Math.max(1f, maxHealth));

        // 背景环（普通混合）
        drawArc(g, cx, cy, radius, thickness, 1f, healthBg, -90, 360);
        if (healthRatio > 0) {
            // 加法混合发光层
            enableAdditiveBlend();
            drawArc(g, cx, cy, radius + 3, thickness + 6,
                    healthRatio, withAlpha(healthClr, 0x22), -90, 360);
            drawArc(g, cx, cy, radius + 2, thickness + 4,
                    healthRatio, withAlpha(healthClr, 0x44), -90, 360);
            drawArc(g, cx, cy, radius + 1, thickness + 2,
                    healthRatio, withAlpha(healthClr, 0x66), -90, 360);
            disableAdditiveBlend();
            // 主填充（普通混合）
            drawArc(g, cx, cy, radius, thickness, healthRatio, healthClr, -90, 360);
            // 顶部高光（普通混合，亮白细线）
            drawArc(g, cx, cy, radius - 1, 1, healthRatio,
                    lighter(healthClr, 0.6f), -90, 360);
        }

        // ── 中环：吸收血量（金色，金苹果，加法混合发光）──
        if (absorption > 0) {
            int absRadius = radius + thickness + gap;
            int absClr = TacHudConfig.argb(vh.healthExtraColor, 0xFFFFD700);
            int absBg = withAlpha(absClr, 0x44);
            drawArc(g, cx, cy, absRadius, thickness, 1f, absBg, -90, 360);
            float absRatio = Math.min(1f, absorption / 20f);
            if (absRatio > 0) {
                enableAdditiveBlend();
                drawArc(g, cx, cy, absRadius + 3, thickness + 6,
                        absRatio, withAlpha(absClr, 0x22), -90, 360);
                drawArc(g, cx, cy, absRadius + 2, thickness + 4,
                        absRatio, withAlpha(absClr, 0x44), -90, 360);
                drawArc(g, cx, cy, absRadius + 1, thickness + 2,
                        absRatio, withAlpha(absClr, 0x66), -90, 360);
                disableAdditiveBlend();
                drawArc(g, cx, cy, absRadius, thickness, absRatio, absClr, -90, 360);
                drawArc(g, cx, cy, absRadius - 1, 1, absRatio,
                        lighter(absClr, 0.6f), -90, 360);
            }
        }

        // ── 外环：护甲（10段，1段=2值，蓝色，加法混合发光）──
        // 即使护甲为0也绘制背景环
        if (vh.armorEnabled) {
            int armorRadius = radius + (thickness + gap) * 2;
            int armorClr = TacHudConfig.argb(vh.armorColor, 0xFF3CB4FF);
            int armorBg = TacHudConfig.argb(vh.armorBgColor, 0xFF1A2A3A);
            float armorRatio = armor / 20f;
            int filledSegs = Math.round(10 * Math.min(1f, Math.max(0f, armorRatio)));
            int segSweep = 36;
            int gapDeg = 2;
            int drawSweep = segSweep - gapDeg;

            // 先画所有背景段（普通混合）
            for (int i = filledSegs; i < 10; i++) {
                int angle = -90 + i * segSweep;
                drawArc(g, cx, cy, armorRadius, thickness, 1f, armorBg, angle, drawSweep);
            }

            // 再画填充段的发光和主体
            if (filledSegs > 0) {
                enableAdditiveBlend();
                for (int i = 0; i < filledSegs; i++) {
                    int angle = -90 + i * segSweep;
                    drawArc(g, cx, cy, armorRadius + 3, thickness + 6,
                            1f, withAlpha(armorClr, 0x22), angle, drawSweep);
                    drawArc(g, cx, cy, armorRadius + 2, thickness + 4,
                            1f, withAlpha(armorClr, 0x44), angle, drawSweep);
                    drawArc(g, cx, cy, armorRadius + 1, thickness + 2,
                            1f, withAlpha(armorClr, 0x66), angle, drawSweep);
                }
                disableAdditiveBlend();
                // 主填充 + 高光（普通混合）
                for (int i = 0; i < filledSegs; i++) {
                    int angle = -90 + i * segSweep;
                    drawArc(g, cx, cy, armorRadius, thickness, 1f, armorClr, angle, drawSweep);
                    drawArc(g, cx, cy, armorRadius - 1, 1,
                            1f, lighter(armorClr, 0.6f), angle, drawSweep);
                }
            }
        }

        // ── 中心数字（血量，加法混合发光）──
        String hpText = String.valueOf((int) health);
        int textWidth = mc.font.width(hpText);
        int textX = cx - textWidth / 2;
        int textY = cy - 4;
        // 加法混合发光文字
        enableAdditiveBlend();
        int glowClr = withAlpha(healthClr, 0x55);
        g.drawString(mc.font, hpText, textX - 1, textY, glowClr, false);
        g.drawString(mc.font, hpText, textX + 1, textY, glowClr, false);
        g.drawString(mc.font, hpText, textX, textY - 1, glowClr, false);
        g.drawString(mc.font, hpText, textX, textY + 1, glowClr, false);
        int glowClr2 = withAlpha(healthClr, 0x30);
        g.drawString(mc.font, hpText, textX - 2, textY, glowClr2, false);
        g.drawString(mc.font, hpText, textX + 2, textY, glowClr2, false);
        g.drawString(mc.font, hpText, textX, textY - 2, glowClr2, false);
        g.drawString(mc.font, hpText, textX, textY + 2, glowClr2, false);
        disableAdditiveBlend();
        // 主文字（普通混合，白色带阴影）
        g.drawString(mc.font, hpText, textX, textY, 0xFFFFFFFF, true);

        // 下方小字
        String maxText = "/" + (int) maxHealth;
        int maxW = mc.font.width(maxText);
        g.drawString(mc.font, maxText,
                cx - maxW / 2, cy + 5, 0xFFAAAAAA, true);
    }

    // ── 加法混合控制 ─────────────────────────────────────────────────

    /** 启用加法混合（SRC_ALPHA, ONE），用于发光效果渲染。 */
    private static void enableAdditiveBlend() {
        RenderSystem.enableBlend();
        // GL_SRC_ALPHA = 0x0302, GL_ONE = 0x0001
        RenderSystem.blendFunc(0x0302, 0x0001);
    }

    /** 恢复默认 Alpha 混合（SRC_ALPHA, ONE_MINUS_SRC_ALPHA）。 */
    private static void disableAdditiveBlend() {
        RenderSystem.defaultBlendFunc();
    }

    // ── 圆弧绘制 ─────────────────────────────────────────────────────

    private static void drawArc(GuiGraphics g, int cx, int cy, int radius,
                                int thickness, float ratio, int color,
                                int startAngle, int sweepAngle) {
        if (ratio <= 0 || radius <= 0 || thickness <= 0) return;
        int innerR = radius - thickness;
        if (innerR < 0) innerR = 0;

        int actualSweep = (int) (sweepAngle * Math.min(1f, Math.max(0f, ratio)));
        if (actualSweep <= 0) return;

        for (int deg = 0; deg < actualSweep; deg++) {
            double angle = Math.toRadians(startAngle + deg);
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            int x1 = cx + (int) Math.round(innerR * cos);
            int y1 = cy + (int) Math.round(innerR * sin);
            int x2 = cx + (int) Math.round(radius * cos);
            int y2 = cy + (int) Math.round(radius * sin);

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minY = Math.min(y1, y2);
            int maxY = Math.max(y1, y2);
            if (minX == maxX) maxX = minX + 1;
            if (minY == maxY) maxY = minY + 1;
            g.fill(minX, minY, maxX, maxY, color);
        }
    }

    // ── 氧气扇形（准星下方，半圆形）───────────────────────────────

    private static void drawAirFan(GuiGraphics g, Minecraft mc,
                                   LocalPlayer p, VanillaHud vh,
                                   double f, int width, int height) {
        int cx = width / 2 + (int) (vh.airOffsetX * f);
        int cy = height / 2 + (int) (vh.airOffsetY * f);

        int radius = Math.max(10, (int) (24 * f));
        int thickness = Math.max(2, (int) (vh.airHeight * f));
        int fanAngle = 120;
        int startAngle = 90 - fanAngle / 2;

        float airRatio = Math.max(0f, (float) p.getAirSupply() / p.getMaxAirSupply());
        int airClr = TacHudConfig.argb(vh.airColor, 0xFF00BFFF);
        int airBg = TacHudConfig.argb(vh.airBgColor, 0xFF152A3A);

        drawArc(g, cx, cy, radius, thickness, 1f, airBg, startAngle, fanAngle);
        if (airRatio > 0) {
            enableAdditiveBlend();
            drawArc(g, cx, cy, radius + 2, thickness + 4,
                    airRatio, withAlpha(airClr, 0x33), startAngle, fanAngle);
            drawArc(g, cx, cy, radius + 1, thickness + 2,
                    airRatio, withAlpha(airClr, 0x55), startAngle, fanAngle);
            disableAdditiveBlend();
            drawArc(g, cx, cy, radius, thickness, airRatio, airClr, startAngle, fanAngle);
            drawArc(g, cx, cy, radius - 1, 1, airRatio,
                    lighter(airClr, 0.5f), startAngle, fanAngle);
        }

        String airText = String.valueOf((int) Math.ceil(airRatio * 100)) + "%";
        int textW = mc.font.width(airText);
        g.drawString(mc.font, airText, cx - textW / 2, cy + radius + (int) (4 * f),
                airClr, true);
    }

    // ── 右侧状态条（饱和度 + 饥饿，垂直堆叠）───────────────

    private static void drawSideBars(GuiGraphics g, Minecraft mc,
                                     LocalPlayer p, VanillaHud vh,
                                     double f, int cx, int cy,
                                     int radius, int thickness) {
        int gap = Math.max(1, (int) (2 * f));
        int armorRadius = radius + (thickness + gap) * 2;

        int barW = Math.max(2, (int) (vh.hungerWidth * f));
        int hungerH = Math.max(2, (int) (vh.hungerHeight * f));
        int satH = Math.max(2, (int) (vh.saturationHeight * f));
        int barGap = Math.max(1, (int) (vh.segmentGap * f));

        int barX = cx + armorRadius + (int) (8 * f) + (int) (vh.hungerOffsetX * f);
        int totalH = hungerH + barGap + satH;
        int startY = cy - totalH / 2 + (int) (vh.hungerOffsetY * f);
        int curY = startY;

        int satW = Math.max(2, (int) (vh.saturationWidth * f));
        float satRatio = Math.min(1f, p.getFoodData().getSaturationLevel() / 20f);
        if (satRatio > 0.01f) {
            int satClr = TacHudConfig.argb(vh.saturationColor, 0xFFFFD700);
            int satBg = TacHudConfig.argb(vh.hungerBgColor, 0xFF3A3015);
            drawTacticalBar(g, barX, curY, satW, satH,
                    satRatio, vh.hungerSegments, satClr, satBg);
        }
        curY += satH + barGap;

        float hungerRatio = Math.min(1f, p.getFoodData().getFoodLevel() / 20f);
        int hClr = TacHudConfig.argb(vh.hungerColor, 0xFFC49C48);
        int hBg = TacHudConfig.argb(vh.hungerBgColor, 0xFF3A2A15);
        drawTacticalBar(g, barX, curY, barW, hungerH,
                hungerRatio, vh.hungerSegments, hClr, hBg);

        String foodText = String.valueOf(p.getFoodData().getFoodLevel());
        int textX = barX + barW + (int) (4 * f);
        g.drawString(mc.font, foodText, textX, curY + hungerH / 2 - 4,
                hClr, true);
    }

    // ── 颜色工具 ─────────────────────────────────────────────────────

    private static int lighter(int argb, float amount) {
        int a = (argb >> 24) & 0xFF;
        int r = Math.min(255, (int) (((argb >> 16) & 0xFF) * (1f + amount)));
        int gr = Math.min(255, (int) (((argb >> 8) & 0xFF) * (1f + amount)));
        int b = Math.min(255, (int) ((argb & 0xFF) * (1f + amount)));
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }

    private static int withAlpha(int argb, int alpha) {
        return ((alpha & 0xFF) << 24) | (argb & 0x00FFFFFF);
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

    private static void drawTacticalBar(GuiGraphics g,
                                        int x, int y, int w, int h,
                                        float ratio, int segments,
                                        int fillColor, int bgColor) {
        g.fill(x, y, x + w, y + h, bgColor);

        int fillW = Math.max(0,
                (int) Math.round(w * Math.min(1f, Math.max(0f, ratio))));
        if (fillW > 0) {
            // 加法混合发光
            enableAdditiveBlend();
            g.fill(x, y - 2, x + fillW, y + h + 2, withAlpha(fillColor, 0x22));
            g.fill(x, y - 1, x + fillW, y + h + 1, withAlpha(fillColor, 0x44));
            disableAdditiveBlend();
            // 主填充
            g.fill(x, y, x + fillW, y + h, fillColor);
            // 顶部高光
            if (h >= 3) {
                g.fill(x, y, x + fillW, y + 1, lighter(fillColor, 0.4f));
            }
        }

        if (segments > 1 && h >= 3) {
            int segW = w / segments;
            for (int i = 1; i < segments; i++) {
                int dx = x + i * segW;
                g.fill(dx, y, dx + 1, y + h, 0x33000000);
            }
        }

        if (h >= 3) {
            g.fill(x, y, x + w, y + 1, 0x66000000);
            g.fill(x, y + h - 1, x + w, y + h, 0x66000000);
        }
        g.fill(x, y, x + 1, y + h, 0x66000000);
        g.fill(x + w - 1, y, x + w, y + h, 0x66000000);
    }
}
