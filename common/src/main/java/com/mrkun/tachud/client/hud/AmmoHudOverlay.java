package com.mrkun.tachud.client.hud;

import com.mrkun.tachud.client.hud.HudScale;
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
 * a colored accent bar, a small label, a primary value and a secondary
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

        double f = HudScale.factor(height, cfg);
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

        int labelH = font.lineHeight;
        int bigH = font.lineHeight;
        int totalH = labelH + 2 + bigH;

        int x = (int) (a.marginX * f);
        int bottom = height - (int) (a.marginY * f);
        int topY = bottom - totalH;
        int textX = x + (int) (6.0 * f);
        int barW = Math.max(1, (int) (2.0 * f));

        // Left accent bar.
        g.fill(x, topY, x + barW, bottom, barColor);
        // Small label.
        g.drawString(font, info.label(), textX, topY, labelColor, false);

        // Primary value. 1.21.8 removed PoseStack scaling, so we draw at native
        // size (it already tracks Minecraft's GUI scale); the accent bar + label
        // keep the COD-style readout intact and now also scale with uiScale.
        g.drawString(font, info.value(), textX, topY + labelH + 2, valueColor, true);
        int bigW = font.width(info.value());

        // Secondary detail, baseline-aligned with the primary value.
        g.drawString(font, info.detail(), textX + bigW + (int) (6.0 * f), bottom - font.lineHeight, labelColor, false);
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
        // 1.21.8: Inventory.items / offhand are no longer public fields.
        // getNonEquipmentItems() returns the combined main + offhand stacks.
        for (ItemStack s : inv.getNonEquipmentItems()) {
            if (s.is(ItemTags.ARROWS)) count += s.getCount();
        }
        return count;
    }
}
