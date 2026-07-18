package com.mrkun.tachud.neoforge.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
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
        // HUD beautification fully reverted — vanilla health / armour / food
        // layers always render normally.
    }
}
