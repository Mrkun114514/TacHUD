package com.mrkun.tachud.fabric.mixin;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels vanilla rendering of health / armour / food / XP bars whenever
 * TacHUD's {@code vanillaHud} replacement is active, so only our COD‑style
 * bars (rendered by {@code VanillaHudOverlay}) appear.
 */
@Mixin(Gui.class)
public class GuiMixin {

    private static boolean isActive() {
        TacHudConfig cfg = ConfigManager.get();
        return cfg.masterEnabled && cfg.vanillaHud.enabled;
    }

    private static boolean isFoodCancelled() {
        TacHudConfig cfg = ConfigManager.get();
        return cfg.masterEnabled && cfg.vanillaHud.enabled
                && cfg.vanillaHud.autoHunger && cfg.vanillaHud.hungerEnabled;
    }

    // ── Health hearts ───────────────────────────────────────────────────

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void tachud$cancelHealth(GuiGraphics graphics, CallbackInfo ci) {
        if (isActive()) ci.cancel();
    }

    // ── Armour icons ────────────────────────────────────────────────────

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void tachud$cancelArmor(GuiGraphics graphics, Player player,
                                           int i, int j, int k, int l,
                                           CallbackInfo ci) {
        if (isActive()) ci.cancel();
    }

    // ── Food drumsticks ─────────────────────────────────────────────────

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void tachud$cancelFood(GuiGraphics graphics, Player player,
                                   int i, int j, CallbackInfo ci) {
        if (isFoodCancelled()) ci.cancel();
    }
}
