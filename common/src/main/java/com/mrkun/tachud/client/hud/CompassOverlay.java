package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.client.hud.HudScale;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

/**
 * Top-center tactical compass: a scrolling heading strip with cardinal /
 * intercardinal labels, degree ticks, a center bearing caret and a numeric
 * heading readout - the classic military-shooter navigation band.
 */
public final class CompassOverlay {

    private CompassOverlay() {
    }

    public static void render(GuiGraphics g, Minecraft mc, LocalPlayer player,
                              TacHudConfig cfg, int width, int height) {
        TacHudConfig.Compass c = cfg.compass;
        if (!c.enabled) return;

        Font font = mc.font;
        double f = HudScale.factor(height, cfg);
        float heading = (player.getYRot() + 180f) % 360f;
        if (heading < 0) heading += 360f;

        int cx = width / 2;
        int halfW = Math.max(20, (int) (c.width * f / 2.0));
        int x1 = cx - halfW;
        int x2 = cx + halfW;
        int y1 = (int) (c.marginY * f);
        int barH = (int) (17.0 * f);
        int y2 = y1 + barH;

        int color = TacHudConfig.argb(c.color, 0xFFFFFFFF);
        int accent = TacHudConfig.argb(c.accentColor, 0xFFFFC400);
        double halfFov = Math.max(20.0, c.fov / 2.0);

        // Strip background with darker feathered ends.
        g.fill(x1, y1, x2, y2, 0xA0000000);
        Draw.gradientH(g, x1, y1, x1 + 12, y2, 0xB0000000, 0x00000000);
        Draw.gradientH(g, x2 - 12, y1, x2, y2, 0x00000000, 0xB0000000);

        for (int deg = 0; deg < 360; deg += 15) {
            float delta = Mth.wrapDegrees(deg - heading);
            if (Math.abs(delta) > halfFov) continue;

            int px = cx + Math.round((float) (delta / halfFov) * halfW);
            boolean cardinal = deg % 90 == 0;
            boolean inter = deg % 45 == 0;
            int tickH = cardinal ? 6 : (inter ? 4 : 3);
            g.fill(px, y1 + 2, px + 1, y1 + 2 + tickH, color);

            String label = labelFor(deg);
            if (label != null) {
                int lw = font.width(label);
                g.drawString(font, label, px - lw / 2, y1 + 9, cardinal ? accent : color, true);
            }
        }

        // Center bearing caret + line.
        g.fill(cx, y1, cx + 1, y2, accent);
        g.fill(cx - 2, y1 - 2, cx + 3, y1 - 1, accent);

        // Numeric heading readout, centered under the strip.
        String hdg = String.format("%03d", Math.round(heading) % 360);
        g.drawCenteredString(font, hdg, cx, y2 + (int) (2.0 * f), accent);
    }

    private static String labelFor(int deg) {
        return switch (deg) {
            case 0 -> "N";
            case 45 -> "NE";
            case 90 -> "E";
            case 135 -> "SE";
            case 180 -> "S";
            case 225 -> "SW";
            case 270 -> "W";
            case 315 -> "NW";
            default -> null;
        };
    }
}
