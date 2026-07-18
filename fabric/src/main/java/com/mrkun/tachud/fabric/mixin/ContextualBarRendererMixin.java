package com.mrkun.tachud.fabric.mixin;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Cancel the XP level text rendered by the contextual‑bar system. */
@Mixin(ContextualBarRenderer.class)
public class ContextualBarRendererMixin {

    @Inject(method = "renderExperienceLevel",
            at = @At("HEAD"), remap = false, cancellable = true)
    private static void tachud$cancelXpLevel(GuiGraphics graphics, Font font,
                                             int level, CallbackInfo ci) {
        TacHudConfig cfg = ConfigManager.get();
        if (cfg.masterEnabled && cfg.vanillaHud.enabled && cfg.vanillaHud.xpBarEnabled) {
            ci.cancel();
        }
    }
}
