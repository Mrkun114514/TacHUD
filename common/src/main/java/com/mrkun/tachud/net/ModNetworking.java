package com.mrkun.tachud.net;

import com.mrkun.tachud.client.ClientPacketHandlers;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;

/**
 * Central registration for TacHUD's server -> client packets, using
 * Architectury's {@link NetworkManager} so a single code path works on both
 * Fabric and NeoForge.
 *
 * <p>The S2C receivers reference client-only classes, but only from inside the
 * deferred {@code context.queue(...)} lambda, so those classes are never loaded
 * on a dedicated server.
 */
public final class ModNetworking {

    private ModNetworking() {
    }

    public static void register() {
        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                KillPayload.TYPE,
                KillPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> ClientPacketHandlers.onKill(payload)));

        NetworkManager.registerReceiver(
                NetworkManager.Side.S2C,
                HitPayload.TYPE,
                HitPayload.STREAM_CODEC,
                (payload, context) -> context.queue(() -> ClientPacketHandlers.onHit(payload)));
    }

    public static void sendKill(ServerPlayer player, String victimName, boolean victimIsPlayer) {
        NetworkManager.sendToPlayer(player, new KillPayload(victimName, victimIsPlayer));
    }

    public static void sendHit(ServerPlayer player, boolean fatal) {
        NetworkManager.sendToPlayer(player, new HitPayload(fatal));
    }
}
