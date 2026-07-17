package com.mrkun.tachud.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrkun.tachud.TacHud;
import com.mrkun.tachud.client.gui.TacHudConfigScreen;
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
 * keybind, a config-screen keybind and the per-tick poll for both. Called
 * from each platform's client initializer.
 */
public final class TacHudClient {

    public static final KeyMapping RELOAD_KEY = new KeyMapping(
            "key.tachud.reload_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "key.categories.tachud");

    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.tachud.open_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G,
            "key.categories.tachud");

    /** Show the "press G" hint once after the first world join. */
    private static boolean hintShown = false;

    private TacHudClient() {
    }

    public static void init() {
        KeyMappingRegistry.register(RELOAD_KEY);
        KeyMappingRegistry.register(OPEN_CONFIG_KEY);

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
        while (OPEN_CONFIG_KEY.consumeClick()) {
            mc.setScreen(new TacHudConfigScreen(mc.screen));
        }
        if (reloaded && mc.player != null) {
            mc.player.displayClientMessage(
                    Component.literal("§a[TacHUD] §fconfig reloaded"), true);
        }
        if (!hintShown && mc.player != null && mc.screen == null) {
            hintShown = true;
            mc.player.displayClientMessage(
                    Component.literal("§a[TacHUD] §f按 G 打开设置，按 R 重载配置"), true);
        }
    }
}
