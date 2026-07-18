package com.mrkun.tachud.neoforge.client;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * NeoForge‑specific event handler that cancels the vanilla health / armour /
 * food / XP layer rendering when TacHUD's replacement bars are active.
 */
@EventBusSubscriber(modid = "tachud")
public final class VanillaHudNeoForge {

    private VanillaHudNeoForge() {
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.vanillaHud.enabled) return;

        ResourceLocation id = event.getName();
        if (id.equals(VanillaGuiLayers.PLAYER_HEALTH) && cfg.vanillaHud.healthEnabled) {
            event.setCanceled(true);
        } else if (id.equals(VanillaGuiLayers.ARMOR_LEVEL) && cfg.vanillaHud.armorEnabled) {
            event.setCanceled(true);
        } else if (id.equals(VanillaGuiLayers.FOOD_LEVEL) && cfg.vanillaHud.hungerEnabled && cfg.vanillaHud.autoHunger) {
            event.setCanceled(true);
        } else if (id.equals(VanillaGuiLayers.EXPERIENCE_LEVEL) && cfg.vanillaHud.xpBarEnabled) {
            event.setCanceled(true);
        } else if (id.equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR) && cfg.vanillaHud.xpBarEnabled) {
            event.setCanceled(true);
        } else if (id.equals(VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND) && cfg.vanillaHud.xpBarEnabled) {
            event.setCanceled(true);
        }
    }
}
