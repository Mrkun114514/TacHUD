package com.mrkun.tachud.net;

import com.mrkun.tachud.TacHud;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Server -> client packet fired when the receiving player lands a hit on a
 * living entity. {@code fatal} marks the killing blow so the client can flash a
 * distinct "kill" hitmarker.
 */
public record HitPayload(boolean fatal) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<HitPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(TacHud.MOD_ID, "hit"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HitPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, HitPayload::fatal,
                    HitPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
