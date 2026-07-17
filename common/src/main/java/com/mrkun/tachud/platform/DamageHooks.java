package com.mrkun.tachud.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;

/**
 * Platform-specific "damage dealt by a player" hook.
 *
 * <p>Architectury's common event bus does not expose a stable cross-version
 * "living hurt" event, so this small surface is implemented per loader:
 * <ul>
 *   <li>Fabric  -> {@code ServerLivingEntityEvents.AFTER_DAMAGE}</li>
 *   <li>NeoForge -> {@code LivingDamageEvent.Post}</li>
 * </ul>
 * Both implementations forward to
 * {@link HitBridge#onPlayerDealtDamage} so the shared hitmarker logic lives in
 * common code.
 */
public final class DamageHooks {

    private DamageHooks() {
    }

    @ExpectPlatform
    public static void init() {
        throw new AssertionError("ExpectPlatform implementation missing");
    }
}
