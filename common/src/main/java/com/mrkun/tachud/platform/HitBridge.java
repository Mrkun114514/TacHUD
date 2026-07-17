package com.mrkun.tachud.platform;

import com.mrkun.tachud.net.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Shared logic invoked by both platform damage hooks. Keeping this in common
 * code guarantees Fabric and NeoForge behave identically: any time a player
 * deals positive damage to a living entity (that isn't themselves), their
 * client is told to flash a hitmarker.
 */
public final class HitBridge {

    private HitBridge() {
    }

    public static void onPlayerDealtDamage(LivingEntity victim, DamageSource source, float amount) {
        if (amount <= 0.0f) {
            return;
        }
        if (victim.level().isClientSide()) {
            return;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof ServerPlayer player && attacker != victim) {
            boolean fatal = !victim.isAlive() || victim.getHealth() <= 0.0f;
            ModNetworking.sendHit(player, fatal);
        }
    }
}
