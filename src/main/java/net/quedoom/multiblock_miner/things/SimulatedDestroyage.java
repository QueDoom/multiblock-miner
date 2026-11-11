package net.quedoom.multiblock_miner.things;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public class SimulatedDestroyage {
    public static int NEXT_ID = -1;

    public final ServerPlayer player;
    public final ServerLevel level;
    public final BlockPos pos;
    private float progress = 0f;
    private int lastStage = -1;

    private final int crackId = --NEXT_ID;
    public final int depth;

    public SimulatedDestroyage(ServerPlayer player, BlockPos pos, int depth) {
        this.player = player;
        this.level = player.level();
        this.pos = pos;
        this.depth = depth;
    }

    public boolean tick() {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            clear();
            return true;
        }

        progress += state.getDestroyProgress(player, level, pos);

        if (progress >= 1f) {
            clear();
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            if (player.gameMode.destroyBlock(pos)) {
                int nextDepth = DestroyageMgr.depthAt(level, pos) + 1;
                for (Direction dir : Direction.values()) {
                    BlockPos next = pos.relative(dir);
                    if (level.getBlockState(next).is(state.getBlock())) {
                        DestroyageMgr.start(player, next, nextDepth);
                    }
                }
            }
            return true;
        }

        int stage = (int) (progress * 10f);
        if (stage != lastStage) {
            level.destroyBlockProgress(crackId, pos, stage);
            lastStage = stage;
        }
        return false;
    }

    public void clear() {
        level.destroyBlockProgress(crackId, pos, -1);
    }
}