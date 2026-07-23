package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import com.mrkun.tachud.config.TacHudConfig.VanillaHud;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;

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
    /** 当前是否处于辉光离屏渲染阶段（该阶段只画亮部、背景透明化）。 */
    private static boolean glowPass = false;
    /** 当前辉光强度（0 = 关闭）。 */
    private static float glowStrength = 0f;
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
        // 实时程序化辉光强度（多层高斯加法混合），关闭则无发光。
        glowStrength = vh.glowEnabled ? (float) vh.glowIntensity : 0f;

        if (vh.centeredLayout) {
            drawCentered(g, mc, p, vh, f, width, height, now);
        } else {
            drawCorner(g, mc, p, vh, f, width, height, now);
        }
    }

    private static void drawElements(GuiGraphics g, Minecraft mc,
                                     LocalPlayer p, VanillaHud vh,
                                     double f, int width, int height, long now) {
        if (vh.centeredLayout) {
            drawCentered(g, mc, p, vh, f, width, height, now);
        } else {
            drawCorner(g, mc, p, vh, f, width, height, now);
        }
    }

    private static void drawCorner(GuiGraphics g, Minecraft mc,
                                   LocalPlayer p, VanillaHud vh,
                                   double f, int width, int height, long now) {
        int sideMargin = Math.max(2, (int) (6 * f));
        int bottomMargin = Math.max(2, (int) (vh.marginBottom * f));

        int radius = Math.max(6, (int) (vh.healthRingRadius * f));
        int thickness = Math.max(2, (int) (vh.healthRingThickness * f));

        int cx = sideMargin + radius + (int) (vh.healthOffsetX * f);
        int cy = height - bottomMargin - radius + (int) (vh.healthOffsetY * f);

        if (vh.healthEnabled) {
            drawConcentricRings(g, mc, p, vh, f, cx, cy, radius, thickness, now, false);
        }

        // 护甲环独立渲染，不依赖血条开关
        if (vh.armorEnabled && !vh.healthEnabled) {
            int armorRadius = radius + (thickness + Math.max(1, (int) (2 * f))) * 2;
            int armorCx = cx + (int) (vh.armorOffsetX * f);
            int armorCy = cy + (int) (vh.armorOffsetY * f);
            drawArmorRing(g, mc, p, vh, f, armorCx, armorCy, armorRadius, thickness, now);
        }

        boolean showAir = vh.airEnabled && p.getAirSupply() < p.getMaxAirSupply();
        boolean showHunger = vh.hungerEnabled && (!vh.autoHunger || !appleskinLoaded);

        if (showAir) {
            drawAirFan(g, mc, p, vh, f, width, height, now);
        }

        if (showHunger) {
            drawSideBars(g, mc, p, vh, f, cx, cy, radius, thickness, now);
        }
    }

    // ── 居中战术布局（概念图效果）──────────────────────────────────────
    // 血环 + 护甲环环绕屏幕中心准星，饱和度在左、饥饿在右、氧气在下。

    private static void drawCentered(GuiGraphics g, Minecraft mc,
                                       LocalPlayer p, VanillaHud vh,
                                       double f, int width, int height, long now) {
        int scx = width / 2;
        int scy = height / 2;

        int radius = Math.max(20, (int) (vh.centeredRingRadius * f));
        int thickness = Math.max(2, (int) (vh.healthRingThickness * f));
        int gap = Math.max(1, (int) (2 * f));

        int cx = scx + (int) (vh.healthOffsetX * f);
        int cy = scy + (int) (vh.healthOffsetY * f);

        // 血环 + 吸收环 + 护甲环（数字绘制在环下方，避免遮挡准星）
        if (vh.healthEnabled) {
            drawConcentricRings(g, mc, p, vh, f, cx, cy, radius, thickness, now, true);
        } else if (vh.armorEnabled) {
            int armorRadius = radius + (thickness + gap) * 2;
            drawArmorRing(g, mc, p, vh, f,
                    cx + (int) (vh.armorOffsetX * f),
                    cy + (int) (vh.armorOffsetY * f),
                    armorRadius, thickness, now);
        }

        // 中心发光十字准星
        if (vh.crosshairEnabled) {
            drawCrosshair(g, vh, f, scx, scy, now);
        }

        // 氧气扇形（准星下方，仅水下）
        boolean showAir = vh.airEnabled && p.getAirSupply() < p.getMaxAirSupply();
        if (showAir) {
            drawAirFan(g, mc, p, vh, f, width, height, now);
        }

        // 饱和度（准星左侧）+ 饥饿（准星右侧）
        boolean showHunger = vh.hungerEnabled && (!vh.autoHunger || !appleskinLoaded);
        if (showHunger) {
            drawCenteredBars(g, mc, p, vh, f, scx, scy, radius, thickness, gap, now);
        }
    }

    /** 居中模式：饱和度条在准星左侧（右对齐向左延伸），饥饿条在准星右侧。 */
    private static void drawCenteredBars(GuiGraphics g, Minecraft mc,
                                         LocalPlayer p, VanillaHud vh,
                                         double f, int scx, int scy,
                                         int radius, int thickness, int gap, long now) {
        int ringOuter = radius + (thickness + gap) * 2;
        int sideGap = (int) (12 * f);

        int hungerH = Math.max(2, (int) (vh.hungerHeight * f));
        int satH = Math.max(2, (int) (vh.saturationHeight * f));
        int barW = Math.max(10, (int) (vh.hungerWidth * f));
        int satW = Math.max(10, (int) (vh.saturationWidth * f));

        // ── 饥饿：准星右侧 ──
        int hungerX = scx + ringOuter + sideGap + (int) (vh.hungerOffsetX * f);
        int hungerY = scy - hungerH / 2 + (int) (vh.hungerOffsetY * f);
        float hungerRatio = Math.min(1f, p.getFoodData().getFoodLevel() / 20f);
        int hClr = TacHudConfig.argb(vh.hungerColor, 0xFFC49C48);
        int hBg = bg(TacHudConfig.argb(vh.hungerBgColor, 0xFF3A2A15));
        drawTacticalBar(g, hungerX, hungerY, barW, hungerH,
                hungerRatio, vh.hungerSegments, hClr, hBg, now);
        String foodText = String.valueOf(p.getFoodData().getFoodLevel());
        g.drawString(mc.font, foodText, hungerX + barW + (int) (4 * f),
                hungerY + hungerH / 2 - 4, hClr, true);

        // ── 饱和度：准星左侧（右对齐，条右端贴近环）──
        float satRatio = Math.min(1f, p.getFoodData().getSaturationLevel() / 20f);
        if (satRatio > 0.01f) {
            int satRight = scx - ringOuter - sideGap + (int) (vh.saturationOffsetX * f);
            int satX = satRight - satW;
            int satY = scy - satH / 2 + (int) (vh.saturationOffsetY * f);
            int satClr = TacHudConfig.argb(vh.saturationColor, 0xFFFFD700);
            int satBg = bg(TacHudConfig.argb(vh.hungerBgColor, 0xFF3A3015));
            drawTacticalBarRTL(g, satX, satY, satW, satH,
                    satRatio, vh.hungerSegments, satClr, satBg, now);
        }
    }

    /** 发光十字准星（四臂 + 中心点，加法混合光晕）。 */
    private static void drawCrosshair(GuiGraphics g, VanillaHud vh,
                                      double f, int cx, int cy, long now) {
        int len = Math.max(2, (int) (vh.crosshairSize * f));
        int gap = Math.max(1, (int) (vh.crosshairGap * f));
        int clr = TacHudConfig.argb(vh.crosshairColor, 0xFFFFFFFF);
        // 主准星
        // 左臂
        g.fill(cx - gap - len, cy, cx - gap, cy + 1, clr);
        // 右臂
        g.fill(cx + gap, cy, cx + gap + len, cy + 1, clr);
        // 上臂
        g.fill(cx, cy - gap - len, cx + 1, cy - gap, clr);
        // 下臂
        g.fill(cx, cy + gap, cx + 1, cy + gap + len, clr);

        // 柔和辉光（高斯多层加法混合）
        if (glowStrength > 0.001f) {
            enableAdditiveBlend();
            int layers = 6;
            float sigma = layers / 2.2f;
            for (int i = 1; i <= layers; i++) {
                float a = glowStrength * (float) Math.exp(-(i * i) / (2f * sigma * sigma));
                int ai = Math.min(255, (int) (a * 255));
                if (ai <= 0) continue;
                int e = i;
                g.fill(cx - gap - len - e, cy - (1 + e), cx - gap + e, cy + (1 + e), withAlpha(clr, ai));
                g.fill(cx + gap - e, cy - (1 + e), cx + gap + len + e, cy + (1 + e), withAlpha(clr, ai));
                g.fill(cx - (1 + e), cy - gap - len - e, cx + (1 + e), cy - gap + e, withAlpha(clr, ai));
                g.fill(cx - (1 + e), cy + gap - e, cx + (1 + e), cy + gap + len + e, withAlpha(clr, ai));
            }
            disableAdditiveBlend();
        }

        // 中心脉冲点
        float pulse = (float) (0.5 + 0.5 * Math.sin(now * 0.004));
        int dotClr = withAlpha(lighter(clr, 0.4f), (int) (0x80 + pulse * 0x7F));
        g.fill(cx, cy, cx + 1, cy + 1, dotClr);
    }

    // ── 同心环绘制（加法混合发光）────────────────────────────────────

    private static void drawConcentricRings(GuiGraphics g, Minecraft mc,
                                            LocalPlayer p, VanillaHud vh,
                                            double f, int cx, int cy,
                                            int radius, int thickness, long now,
                                            boolean centered) {
        float health = p.getHealth();
        float maxHealth = p.getMaxHealth();
        float absorption = p.getAbsorptionAmount();

        int gap = Math.max(1, (int) (2 * f));

        // ── 护甲环在血量环内绘制（与血条共享位置时）──
        if (vh.armorEnabled) {
            float armor = p.getArmorValue();
            int armorRadius = radius + (thickness + gap) * 2;
            drawArmorRingImpl(g, vh, f, cx, cy, armorRadius, thickness, armor, now);
        }

        // ── 内环：血量（红色，发光）──
        int healthBg = bg(TacHudConfig.argb(vh.healthBgColor, 0xFF3A1515));
        int healthClr = TacHudConfig.argb(vh.healthColor, 0xFFCC0000);
        float healthRatio = Math.min(1f, health / Math.max(1f, maxHealth));

        drawArc(g, cx, cy, radius, thickness, 1f, healthBg, -90, 360);
        if (healthRatio > 0) {
            // 主填充
            drawArc(g, cx, cy, radius, thickness, healthRatio, healthClr, -90, 360);
            // 顶部高光
            drawArc(g, cx, cy, radius - 1, 1, healthRatio,
                    lighter(healthClr, 0.6f), -90, 360);
            // 发光
            drawGlowArc(g, cx, cy, radius, thickness, healthRatio, healthClr, -90, 360);
        }

        // ── 中环：吸收血量（金色，金苹果，发光）──
        if (absorption > 0) {
            int absRadius = radius + thickness + gap;
            int absClr = TacHudConfig.argb(vh.healthExtraColor, 0xFFFFD700);
            int absBg = bg(withAlpha(absClr, 0x44));
            drawArc(g, cx, cy, absRadius, thickness, 1f, absBg, -90, 360);
            float absRatio = Math.min(1f, absorption / 20f);
            if (absRatio > 0) {
                drawArc(g, cx, cy, absRadius, thickness, absRatio, absClr, -90, 360);
                drawArc(g, cx, cy, absRadius - 1, 1, absRatio,
                        lighter(absClr, 0.6f), -90, 360);
                // 发光
                drawGlowArc(g, cx, cy, absRadius, thickness, absRatio, absClr, -90, 360);
            }
        }

        // ── 血量数字（真实发光）──
        // 居中布局：文字放在环正下方外侧，避免遮挡准星；
        // 角落布局：文字在环中心。
        String hpText = String.valueOf((int) health);
        int textWidth = mc.font.width(hpText);
        int textX = cx - textWidth / 2;
        int textY = centered ? (cy + radius + thickness + (int) (4 * f)) : (cy - 4);
        // 加法混合发光文字
        enableAdditiveBlend();
        int glowClr = withAlpha(healthClr, 0x60);
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
        // 主文字
        g.drawString(mc.font, hpText, textX, textY, 0xFFFFFFFF, true);

        // 下方小字（最大血量）
        String maxText = "/" + (int) maxHealth;
        int maxW = mc.font.width(maxText);
        g.drawString(mc.font, maxText,
                cx - maxW / 2, textY + 10, 0xFFAAAAAA, true);
    }

    /** 独立护甲环渲染（不依赖血条开关）。 */
    private static void drawArmorRing(GuiGraphics g, Minecraft mc,
                                       LocalPlayer p, VanillaHud vh,
                                       double f, int cx, int cy,
                                       int radius, int thickness, long now) {
        float armor = p.getArmorValue();
        drawArmorRingImpl(g, vh, f, cx, cy, radius, thickness, armor, now);
    }

    /** 护甲环实际绘制逻辑，复用于共享位置和独立位置两种场景。 */
    private static void drawArmorRingImpl(GuiGraphics g, VanillaHud vh,
                                           double f, int cx, int cy,
                                           int armorRadius, int thickness,
                                           float armor, long now) {
        int armorClr = TacHudConfig.argb(vh.armorColor, 0xFF3CB4FF);
        int armorBg = bg(TacHudConfig.argb(vh.armorBgColor, 0xFF1A2A3A));
        float armorRatio = armor / 20f;
        int filledSegs = Math.round(10 * Math.min(1f, Math.max(0f, armorRatio)));
        int segSweep = 36;
        int gapDeg = 2;
        int drawSweep = segSweep - gapDeg;

        // 绘制未填充段的暗色背景
        for (int i = filledSegs; i < 10; i++) {
            int angle = -90 + i * segSweep;
            drawArc(g, cx, cy, armorRadius, thickness, 1f, armorBg, angle, drawSweep);
        }

        // 全圈背景暗色环（确保空段可见）
        if (filledSegs < 10) {
            enableAdditiveBlend();
            drawArc(g, cx, cy, armorRadius + 1, thickness, 1f,
                    withAlpha(armorBg, 0x20), -90, 360);
            disableAdditiveBlend();
        }

        if (filledSegs > 0) {
            // 主填充 + 高光
            for (int i = 0; i < filledSegs; i++) {
                int angle = -90 + i * segSweep;
                drawArc(g, cx, cy, armorRadius, thickness, 1f, armorClr, angle, drawSweep);
                drawGlowArc(g, cx, cy, armorRadius, thickness, 1f, armorClr, angle, drawSweep);
                drawArc(g, cx, cy, armorRadius - 1, 1, 1f,
                        lighter(armorClr, 0.6f), angle, drawSweep);
            }
        }
    }

    // ── 加法混合控制（真实发光，LWJGL原生）──────────────────────────────

    private static void enableAdditiveBlend() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
    }

    private static void disableAdditiveBlend() {
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    /** 平滑辉光：以高斯衰减叠加多层圆弧，向内外两侧柔和扩散（加法混合）。 */
    private static void drawGlowArc(GuiGraphics g, int cx, int cy, int radius, int thickness,
                                    float ratio, int color, int start, int sweep) {
        if (glowStrength <= 0.001f || ratio <= 0f) return;
        enableAdditiveBlend();
        int layers = 8;
        float sigma = layers / 2.4f;
        for (int i = 1; i <= layers; i++) {
            float a = glowStrength * (float) Math.exp(-(i * i) / (2f * sigma * sigma));
            int ai = Math.min(255, (int) (a * 255));
            if (ai <= 0) continue;
            drawArc(g, cx, cy, radius, thickness + i, ratio, withAlpha(color, ai), start, sweep);
            int tin = thickness - i;
            if (tin > 0) drawArc(g, cx, cy, radius, tin, ratio, withAlpha(color, ai), start, sweep);
        }
        disableAdditiveBlend();
    }

    /** 平滑辉光：以高斯衰减叠加多层矩形，向四周柔和扩散（加法混合）。 */
    private static void drawGlowRect(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (glowStrength <= 0.001f) return;
        enableAdditiveBlend();
        int layers = 7;
        float sigma = layers / 2.4f;
        for (int i = 1; i <= layers; i++) {
            float a = glowStrength * (float) Math.exp(-(i * i) / (2f * sigma * sigma));
            int ai = Math.min(255, (int) (a * 255));
            if (ai <= 0) continue;
            g.fill(x - i, y - i, x + w + i, y + h + i, withAlpha(color, ai));
        }
        disableAdditiveBlend();
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
                                   double f, int width, int height, long now) {
        int cx = width / 2 + (int) (vh.airOffsetX * f);
        int cy = height / 2 + (int) (vh.airOffsetY * f);

        int radius = Math.max(10, (int) (24 * f));
        int thickness = Math.max(2, (int) (vh.airHeight * f));
        int fanAngle = 120;
        int startAngle = 90 - fanAngle / 2;

        float airRatio = Math.max(0f, (float) p.getAirSupply() / p.getMaxAirSupply());
        int airClr = TacHudConfig.argb(vh.airColor, 0xFF00BFFF);
        int airBg = bg(TacHudConfig.argb(vh.airBgColor, 0xFF152A3A));

        drawArc(g, cx, cy, radius, thickness, 1f, airBg, startAngle, fanAngle);
        if (airRatio > 0) {
            drawArc(g, cx, cy, radius, thickness, airRatio, airClr, startAngle, fanAngle);
            drawArc(g, cx, cy, radius - 1, 1, airRatio,
                    lighter(airClr, 0.5f), startAngle, fanAngle);
            // 发光
            drawGlowArc(g, cx, cy, radius, thickness, airRatio, airClr, startAngle, fanAngle);
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
                                     int radius, int thickness, long now) {
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
            int satBg = bg(TacHudConfig.argb(vh.hungerBgColor, 0xFF3A3015));
            drawTacticalBar(g, barX, curY, satW, satH,
                    satRatio, vh.hungerSegments, satClr, satBg, now);
        }
        curY += satH + barGap;

        float hungerRatio = Math.min(1f, p.getFoodData().getFoodLevel() / 20f);
        int hClr = TacHudConfig.argb(vh.hungerColor, 0xFFC49C48);
        int hBg = bg(TacHudConfig.argb(vh.hungerBgColor, 0xFF3A2A15));
        drawTacticalBar(g, barX, curY, barW, hungerH,
                hungerRatio, vh.hungerSegments, hClr, hBg, now);

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

    /** 辉光阶段把背景色置为透明，避免暗色背景被一起模糊叠加到世界上。 */
    private static int bg(int c) {
        return glowPass ? 0 : c;
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
                                        int fillColor, int bgColor, long now) {
        g.fill(x, y, x + w, y + h, bg(bgColor));

        int fillW = Math.max(0,
                (int) Math.round(w * Math.min(1f, Math.max(0f, ratio))));
        if (fillW > 0) {
            // 光泽脉冲（正弦波浪高亮扫描）
            float pulse = (float) (0.10 + 0.08 * Math.sin(now * 0.004 + x * 0.01));
            if (pulse > 0.05f) {
                enableAdditiveBlend();
                g.fill(x, y, x + fillW, y + h,
                        withAlpha(lighter(fillColor, 0.3f), (int) (pulse * 255)));
                disableAdditiveBlend();
            }

            // 主填充
            g.fill(x, y, x + fillW, y + h, fillColor);
            // 顶部高光
            if (h >= 3) {
                g.fill(x, y, x + fillW, y + 1, lighter(fillColor, 0.5f));
            }
            // 中间扫描线高光
            if (h >= 5 && fillW > 4) {
                g.fill(x, y + h / 2 - 1, x + fillW, y + h / 2 + 1,
                        lighter(fillColor, 0.2f));
            }
            drawGlowRect(g, x, y, fillW, h, fillColor);
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

    /** 右对齐战术条：填充部分从右端向左延伸（居中布局的左侧饱和度条）。 */
    private static void drawTacticalBarRTL(GuiGraphics g,
                                           int x, int y, int w, int h,
                                           float ratio, int segments,
                                           int fillColor, int bgColor, long now) {
        g.fill(x, y, x + w, y + h, bg(bgColor));

        int fillW = Math.max(0,
                (int) Math.round(w * Math.min(1f, Math.max(0f, ratio))));
        if (fillW > 0) {
            int fx = x + w - fillW; // 填充起点（靠右）

            float pulse = (float) (0.10 + 0.08 * Math.sin(now * 0.004 + x * 0.01));
            if (pulse > 0.05f) {
                enableAdditiveBlend();
                g.fill(fx, y, x + w, y + h,
                        withAlpha(lighter(fillColor, 0.3f), (int) (pulse * 255)));
                disableAdditiveBlend();
            }

            g.fill(fx, y, x + w, y + h, fillColor);
            if (h >= 3) {
                g.fill(fx, y, x + w, y + 1, lighter(fillColor, 0.5f));
            }
            if (h >= 5 && fillW > 4) {
                g.fill(fx, y + h / 2 - 1, x + w, y + h / 2 + 1,
                        lighter(fillColor, 0.2f));
            }
            // 发光
            drawGlowRect(g, fx, y, fillW, h, fillColor);
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
