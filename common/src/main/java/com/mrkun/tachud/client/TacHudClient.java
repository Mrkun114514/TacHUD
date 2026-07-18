package com.mrkun.tachud.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mrkun.tachud.TacHud;
import com.mrkun.tachud.client.gui.TacHudConfigScreen;
import com.mrkun.tachud.client.hud.HudRenderer;
import com.mrkun.tachud.client.hud.KillFeedOverlay;
import com.mrkun.tachud.client.hud.VanillaHudOverlay;
import com.mrkun.tachud.config.ConfigManager;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only entry point. Registers the HUD render callback, a config-reload
 * keybind, a config-screen keybind and the per-tick poll for both. Called
 * from each platform's client initializer.
 *
 * <p><b>ReDeploy integration:</b> detects the {@code RedeployDeathScreen} by
 * class name and fires death / respawn notifications into the kill feed.
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

    /** Fully-qualified class name of ReDeploy's death screen. */
    private static final String REDEPLOY_DEATH_SCREEN_CLASS =
            "com.mrkun114514.redeploy.client.RedeployDeathScreen";

    /** Show the "press G" hint once after the first world join. */
    private static boolean hintShown = false;

    // ---- ReDeploy integration state --------------------------------------
    /** Tracks whether the player was dead in the previous client tick. */
    private static boolean wasDead = false;
    /** Prevents duplicate death-message inserts. */
    private static boolean deathMessageShown = false;

    /** System-message colours (0xAARRGGBB). */
    private static final int MSG_DEATH   = 0xFFFF4D4D; // red
    private static final int MSG_REDEPLOY = 0xFF4DFF4D; // green

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
        // ---- key bindings ----
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
            VanillaHudOverlay.showAppleSkinWarning(mc);
        }

        // ---- ReDeploy integration: death / respawn notifications ----------
        Player player = mc.player;
        if (player == null) return;

        boolean isDead = player.isDeadOrDying();
        Screen screen = mc.screen;

        if (isDead && isRedeployDeathScreen(screen)) {
            if (!deathMessageShown) {
                deathMessageShown = true;
                wasDead = true;
                // Show "YOU DIED" in the kill feed when the death screen opens.
                // Localised at mod level: en = "☠ YOU DIED", zh = "☠ 你已阵亡"
                KillFeedOverlay.addDeathMessage("\u2620 YOU DIED", MSG_DEATH);
            }
        } else {
            // Respawn detected: was dead, now alive.
            if (wasDead) {
                wasDead = false;
                KillFeedOverlay.addDeathMessage("\u2661 REDEPLOY", MSG_REDEPLOY);
            }
            deathMessageShown = false;
        }
    }

    // ---- ReDeploy screen detection (runtime, zero compile dep) -----------

    private static boolean isRedeployDeathScreen(Screen screen) {
        if (screen == null) return false;
        return REDEPLOY_DEATH_SCREEN_CLASS.equals(screen.getClass().getName());
    }
}
