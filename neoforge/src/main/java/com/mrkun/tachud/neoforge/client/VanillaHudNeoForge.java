package com.mrkun.tachud.neoforge.client;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.config.TacHudConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * NeoForge‑specific event handler that cancels the vanilla health / armour /
 * food / air / XP layer rendering when TacHUD's replacement bars are active.
 */
@EventBusSubscriber(modid = "tachud")
public final class VanillaHudNeoForge {

    private VanillaHudNeoForge() {
    }

    @SubscribeEvent
    public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        TacHudConfig cfg = ConfigManager.get();
        if (!cfg.masterEnabled || !cfg.vanillaHud.enabled) return;

        String layerName = event.getLayer().toString();
        switch (layerName) {
            case "PLAYER_HEALTH" -> {
                if (cfg.vanillaHud.healthEnabled) event.setCanceled(true);
            }
            case "PLAYER_ARMOR" -> {
                if (cfg.vanillaHud.armorEnabled) event.setCanceled(true);
            }
            case "PLAYER_FOOD" -> {
                if (cfg.vanillaHud.hungerEnabled && (!cfg.vanillaHud.autoHunger || !isAppleskinLoaded())) {
                    event.setCanceled(true);
                }
            }
            case "PLAYER_AIR" -> {
                if (cfg.vanillaHud.airEnabled) event.setCanceled(true);
            }
            case "EXPERIENCE" -> {
                if (cfg.vanillaHud.xpBarEnabled) event.setCanceled(true);
            }
        }
    }

    private static boolean isAppleskinLoaded() {
        try {
            Class.forName("squeek.appleskin.AppleSkin");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
