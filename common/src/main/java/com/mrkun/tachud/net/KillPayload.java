package com.mrkun.tachud.net;

import com.mrkun.tachud.TacHud;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client packet fired when the receiving player scores a kill.
 *
 * @param victimName   display name of the slain entity
 * @param victimIsPlayer whether the victim was another player (drives color)
 */
public record KillPayload(String victimName, boolean victimIsPlayer) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<KillPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TacHud.MOD_ID, "kill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, KillPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, KillPayload::victimName,
                    ByteBufCodecs.BOOL, KillPayload::victimIsPlayer,
                    KillPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
