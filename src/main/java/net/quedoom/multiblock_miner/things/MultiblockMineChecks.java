package net.quedoom.multiblock_miner.things;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.quedoom.multiblock_miner.ModLoaded;
import net.quedoom.multiblock_miner.config.QMMConfigManager;
import net.quedoom.multiblock_miner.networking.MultimineState;
import net.quedoom.quet.misc.GetPath;

public class MultiblockMineChecks extends CrazyLogger {
    public static boolean enchantment(Level level, ItemStack destroyedWith) {
        return true;
    }

    public static boolean isHoldingKeybind(ServerPlayer serverPlayer) {
        return MultimineState.isActive(serverPlayer);
    }

    public static boolean doItAnywaysBecauseThisIsAnOre(BlockState state, Player player) {
        boolean crouching = player.isCrouching();
        String name = GetPath.get(state.getBlock());
        return switch (QMMConfigManager.CONFIG.ignoreKeybindIfOre) {
            case MULTI_IF_SHIFT -> crouching && (name.contains("ore") || name.equals("ancient_debris"));
            case MULTI_UNLESS_SHIFT -> !crouching && (name.contains("ore") || name.equals("ancient_debris"));
            case ALWAYS_MULTI -> name.contains("ore") || name.equals("ancient_debris");
            case NEVER_MULTI -> false;
        };
    }

    // Making sure it doesn't try to break unbreakable blocks forever.
    public static boolean canBreak(ServerPlayer player, ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (QMMConfigManager.CONFIG.requireCorrectTool) {
            ItemStack tool = player.getMainHandItem();
            if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state)) return false;
        }

        if (state.isAir()) return false;
        info("Isn't air");
        if (state.getDestroySpeed(level, pos) < 0) return false; // Bedrock etc (unless francium)
        info("Not unbreakable");
        if (!player.mayInteract(level, pos)) return false; // Border and spawnprotection
        info("Inside border and not spawn protected");
        if (player.blockActionRestricted(level, pos, player.gameMode.getGameModeForPlayer())) return false; // adventure or spectator
        info("Isn't restricted");
        if (!player.getMainHandItem().canDestroyBlock(state, level, pos, player)) return false;  // ex sword in creative
        info("Can Mine");
        return true;
    }

    // I want it to be harder in francium :evil_smile:
    public static int getMaxDepth() {
        if (ModLoaded.FRANCIUM2 && QMMConfigManager.CONFIG.maxDepth == 4) {
            return 2;
        } else return QMMConfigManager.CONFIG.maxDepth;
    }

}
