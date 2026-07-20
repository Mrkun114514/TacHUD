package com.mrkun.tachud.fabric.mixin;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.client.gui.ChatComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Shifts the chat box upward to avoid overlapping with the tactical HUD.
 * Both rendering AND interaction coordinates are adjusted by the offset.
 */
@Mixin(ChatComponent.class)
public class ChatMixin {

    private static double getChatOffset() {
        TacHudConfig cfg = ConfigManager.get();
        if (cfg.masterEnabled && cfg.vanillaHud.enabled && cfg.vanillaHud.chatEnabled) {
            return cfg.vanillaHud.chatOffsetY;
        }
        return 0;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void tachud$pushChatUp(GuiGraphics g, int tickCount, int width, int height,
                                   boolean focused, CallbackInfo ci) {
        double offset = getChatOffset();
        if (offset != 0) {
            g.pose().pushMatrix();
            g.pose().translate(0f, -(float) offset);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void tachud$popChatUp(GuiGraphics g, int tickCount, int width, int height,
                                  boolean focused, CallbackInfo ci) {
        double offset = getChatOffset();
        if (offset != 0) {
            g.pose().popMatrix();
        }
    }

    @ModifyArgs(method = "getMessageAt", at = @At(value = "HEAD"))
    private void tachud$adjustMessageAtY(Args args) {
        double offset = getChatOffset();
        if (offset != 0) {
            double mouseY = args.get(1);
            args.set(1, mouseY + offset);
        }
    }
}
