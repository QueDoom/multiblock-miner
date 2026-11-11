package net.quedoom.multiblock_miner.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.quedoom.multiblock_miner.QSMultiblockMiner;

public record MultimineStatePayload(boolean active) implements CustomPacketPayload {
    public static final Type<MultimineStatePayload> TYPE =
            new Type<>(QSMultiblockMiner.id("multimine_state"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultimineStatePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, MultimineStatePayload::active,
                    MultimineStatePayload::new
            );

    @Override
    public Type<MultimineStatePayload> type() {
        return TYPE;
    }
}
