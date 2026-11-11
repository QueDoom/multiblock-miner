package net.quedoom.multiblock_miner.networking;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MultimineState {
    private static final Set<UUID> ACTIVE = new HashSet<>();

    public static boolean isActive(ServerPlayer player) {
        return ACTIVE.contains(player.getUUID());
    }

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(MultimineStatePayload.TYPE, MultimineStatePayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(MultimineStatePayload.TYPE, (payload, context) -> {
            if (payload.active()) ACTIVE.add(context.player().getUUID());
            else ACTIVE.remove(context.player().getUUID());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                ACTIVE.remove(handler.getPlayer().getUUID()));
    }
}
