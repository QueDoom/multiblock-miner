package net.quedoom.multiblock_miner.things;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.quedoom.multiblock_miner.QSMultiblockMiner;

public class SimulatedDestroyage extends CrazyLogger{
    public static int NEXT_ID = -1;

    public final ServerPlayer player;
    public final ServerLevel level;
    public final BlockPos worldPosition;
    private float progress = 0f;
    private int lastStage = -1;

    // It is negative so it's never the same as a real id.
    private final int crackId = --NEXT_ID;
    public final int depth;

    public SimulatedDestroyage(ServerPlayer player, BlockPos pos, int depth) {
        this.player = player;
        this.level = player.level();
        this.worldPosition = pos;
        this.depth = depth;
    }

    public boolean tick() {
        BlockState state = level.getBlockState(worldPosition);
        info("Tick check");
        if (!MultiblockMineChecks.canBreak(player, level, worldPosition)) {
            clear();
            return true;
        }

        progress += state.getDestroyProgress(player, level, worldPosition);

        // Break block
        if (progress >= 1f) {
            clear();
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, worldPosition, Block.getId(state));
            if (player.gameMode.destroyBlock(worldPosition)) {
                int nextDepth = DestroyageMgr.depthAt(level, worldPosition) + 1;
                for (Direction dir : Direction.values()) {
                    BlockPos next = worldPosition.relative(dir);
                    if (level.getBlockState(next).is(state.getBlock())) {
                        DestroyageMgr.start(player, next, nextDepth);
                    }
                }
            } else {
                QSMultiblockMiner.LOGGER.info("Something made it so the block at {} couldn't be broken. If you have claims mods this is fine and you can ignore this :)", worldPosition);
            }
            return true;
        }

        info("Progress");
        // Set progress for the break thing
        int stage = (int) (progress * 10f);
        if (stage != lastStage) {
            level.destroyBlockProgress(crackId, worldPosition, stage);
            lastStage = stage;
        }
        return false;
    }

    public void clear() {
        level.destroyBlockProgress(crackId, worldPosition, -1);
    }
}