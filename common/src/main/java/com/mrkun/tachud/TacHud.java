package com.mrkun.tachud;

import com.mrkun.tachud.config.ConfigManager;
import com.mrkun.tachud.net.ModNetworking;
import com.mrkun.tachud.platform.DamageHooks;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (loader-agnostic) entry point for TacHUD.
 *
 * <p>Called from both the Fabric and NeoForge platform initializers so that all
 * gameplay-side logic (kill detection, networking) is registered exactly once
 * per side, identically on both loaders.
 */
public final class TacHud {

    public static final String MOD_ID = "tachud";
    public static final String MOD_NAME = "TacHUD";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private TacHud() {
    }

    /** Invoked by each platform's main mod initializer (client + server). */
    public static void init() {
        ConfigManager.load();
        ModNetworking.register();
        DamageHooks.init();
        registerKillDetection();
        LOGGER.info("[{}] common init complete", MOD_NAME);
    }

    /**
     * Server-authoritative kill detection. When a living entity dies and its
     * killer is a player, we notify that player's client so it can pop a kill
     * feed entry. Entities killed by non-players are intentionally ignored, per
     * design (only the local player's own kills are shown).
     */
    private static void registerKillDetection() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity.level().isClientSide()) {
                return EventResult.pass();
            }
            Entity killer = source.getEntity(); // owner of the projectile / direct attacker
            if (killer instanceof ServerPlayer shooter && killer != entity) {
                boolean victimIsPlayer = entity instanceof Player;
                String victimName = entity.getDisplayName() != null
                        ? entity.getDisplayName().getString()
                        : entity.getName().getString();
                ModNetworking.sendKill(shooter, victimName, victimIsPlayer);
            }
            return EventResult.pass();
        });
    }
}
