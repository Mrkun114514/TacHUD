package com.mrkun.tachud.platform.fabric;

import com.mrkun.tachud.platform.HitBridge;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/**
 * Fabric implementation of {@code DamageHooks.init()} (resolved by Architectury's
 * {@code @ExpectPlatform}). Uses Fabric API's {@code AFTER_DAMAGE} to forward
 * player-dealt damage into the shared {@link HitBridge}.
 */
public final class DamageHooksImpl {

    private DamageHooksImpl() {
    }

    public static void init() {
        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamageTaken, damageTaken, blocked) ->
                        HitBridge.onPlayerDealtDamage(entity, source, damageTaken));
    }
}
