package com.mrkun.tachud.platform.neoforge;

import com.mrkun.tachud.platform.HitBridge;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/**
 * NeoForge implementation of {@code DamageHooks.init()} (resolved by
 * Architectury's {@code @ExpectPlatform}). Listens to {@code LivingDamageEvent.Post}
 * and forwards player-dealt damage into the shared {@link HitBridge}.
 */
public final class DamageHooksImpl {

    private DamageHooksImpl() {
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Post event) ->
                HitBridge.onPlayerDealtDamage(event.getEntity(), event.getSource(), event.getNewDamage()));
    }
}
