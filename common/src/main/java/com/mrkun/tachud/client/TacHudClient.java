package com.mrkun.tachud.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrkun.tachud.TacHud;
import com.mrkun.tachud.client.hud.HudRenderer;
import com.mrkun.tachud.config.ConfigManager;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * Client-only entry point. Registers the HUD render callback, a config-reload
 * keybind and the per-tick poll for it. Called from each platform's client
 * initializer.
 */
public final class TacHudClient {

    public static final KeyMapping RELOAD_KEY = new KeyMapping(
            "key.tachud.reload_config",
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            "key.categories.tachud");

    private TacHudClient() {
    }

    public static void init() {
        KeyMappingRegistry.register(RELOAD_KEY);

        ClientGuiEvent.RENDER_HUD.register((graphics, tickDelta) ->
                HudRenderer.render(graphics));

        ClientTickEvent.CLIENT_POST.register(TacHudClient::onClientTick);

        TacHud.LOGGER.info("[{}] client init complete", TacHud.MOD_NAME);
    }

    private static void onClientTick(Minecraft mc) {
        boolean reloaded = false;
        while (RELOAD_KEY.consumeClick()) {
            ConfigManager.load();
            reloaded = true;
        }
        if (reloaded && mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§a[TacHUD] §fconfig reloaded"), true);
        }
    }
}
