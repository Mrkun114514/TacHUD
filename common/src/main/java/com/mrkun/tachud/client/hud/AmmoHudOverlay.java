package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * Bottom-left ammo / durability readout, laid out like a COD ammo counter:
 * a colored accent bar, a small label, a large primary value and a secondary
 * detail line.
 *
 * <ul>
 *   <li>Bows / crossbows / other projectile weapons -> arrows remaining.</li>
 *   <li>Damageable tools & melee weapons -> durability percentage + raw count.</li>
 * </ul>
 */
public final class AmmoHudOverlay {

    private AmmoHudOverlay() {
    }

    private record Info(String label, String value, String detail, boolean low) {
    }

    public static void render(GuiGraphics g, Minecraft mc, LocalPlayer player,
                              TacHudConfig cfg, int width, int height) {
        TacHudConfig.AmmoHud a = cfg.ammoHud;
        if (!a.enabled) return;

        Info info = analyze(player.getMainHandItem(), player, a);
        if (info == null && a.useOffhandFallback) {
            info = analyze(player.getOffhandItem(), player, a);
        }
        if (info == null) return;

        Font font = mc.font;
        int accent = TacHudConfig.argb(a.accentColor, 0xFFFFC400);
        int low = TacHudConfig.argb(a.lowColor, 0xFFFF3B30);
        int textColor = TacHudConfig.argb(a.textColor, 0xFFFFFFFF);
        int labelColor = 0xFFB8B8B8;
        int barColor = info.low() ? low : accent;
        int valueColor = info.low() ? low : textColor;

        final float scale = 1.7f;
        int labelH = font.lineHeight;
        int bigH = Math.round(font.lineHeight * scale);
        int totalH = labelH + 2 + bigH;

        int x = a.marginX;
        int bottom = height - a.marginY;
        int topY = bottom - totalH;
        int textX = x + 6;

        // Left accent bar.
        g.fill(x, topY, x + 2, bottom, barColor);
        // Small label.
        g.drawString(font, info.label(), textX, topY, labelColor, false);

        // Large primary value.
        g.pose().pushPose();
        g.pose().translate(textX, topY + labelH + 2, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font, info.value(), 0, 0, valueColor, true);
        g.pose().popPose();

        // Secondary detail, baseline-aligned with the big value.
        int bigW = Math.round(font.width(info.value()) * scale);
        g.drawString(font, info.detail(), textX + bigW + 6, bottom - font.lineHeight, labelColor, false);
    }

    private static Info analyze(ItemStack stack, LocalPlayer player, TacHudConfig.AmmoHud cfg) {
        if (stack.isEmpty()) return null;

        if (stack.getItem() instanceof ProjectileWeaponItem) {
            int arrows = countArrows(player);
            return new Info("AMMO", Integer.toString(arrows),
                    stack.getHoverName().getString(), arrows <= 0);
        }

        if (stack.isDamageableItem()) {
            int max = stack.getMaxDamage();
            int remaining = max - stack.getDamageValue();
            int pct = Math.max(0, Math.round(remaining * 100f / max));
            String value = cfg.showDurabilityPercent ? pct + "%" : Integer.toString(remaining);
            return new Info("DURABILITY", value, remaining + " / " + max, pct <= 15);
        }

        return null;
    }

    private static int countArrows(LocalPlayer player) {
        Inventory inv = player.getInventory();
        int count = 0;
        for (ItemStack s : inv.items) {
            if (s.is(ItemTags.ARROWS)) count += s.getCount();
        }
        for (ItemStack s : inv.offhand) {
            if (s.is(ItemTags.ARROWS)) count += s.getCount();
        }
        return count;
    }
}
