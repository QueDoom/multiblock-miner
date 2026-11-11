package net.quedoom.multiblock_miner.things;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.quedoom.multiblock_miner.ModLoaded;
import net.quedoom.multiblock_miner.config.QMMConfigManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DestroyageMgr {
    private static final List<SimulatedDestroyage> ACTIVE = new ArrayList<>();
    private static final List<SimulatedDestroyage> PENDING = new ArrayList<>();
    private static final Map<GlobalPos, Integer> DEPTHS = new HashMap<>();

    public static void start(ServerPlayer player, BlockPos pos, int depth) {
        if (depth > getMaxDepth()) return;
        GlobalPos key = GlobalPos.of(player.level().dimension(), pos.immutable());
        if (DEPTHS.putIfAbsent(key, depth) != null) return;
        PENDING.add(new SimulatedDestroyage(player, pos, depth));
    }

    public static int getMaxDepth() {
        if (ModLoaded.FRANCIUM2 && QMMConfigManager.CONFIG.maxDepth == 4) {
            return 2;
        } else return QMMConfigManager.CONFIG.maxDepth;
    }

    public static int depthAt(Level level, BlockPos pos) {
        return DEPTHS.getOrDefault(GlobalPos.of(level.dimension(), pos), 0);
    }

    static void finished(SimulatedDestroyage destroyage) {
        DEPTHS.remove(GlobalPos.of(destroyage.level.dimension(), destroyage.worldPosition));
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ACTIVE.addAll(PENDING);
            PENDING.clear();

            for (int i = 0; i < ACTIVE.size(); i++) {
                if (ACTIVE.get(i).tick()) {
                    finished(ACTIVE.get(i));
                    ACTIVE.remove(i--);
                }
            }
        });
    }
}
