package net.quedoom.multiblock_miner.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.quedoom.multiblock_miner.QSMultiblockMiner;
import net.quedoom.multiblock_miner.things.DestroyageMgr;
import net.quedoom.multiblock_miner.things.MultiblockMineChecks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class PlayerDestroyBlockMixin {
    @Inject(method = "playerDestroy", at = @At("HEAD"))
    private void multimine(Level level, Player player, BlockPos pos, BlockState state,
                           BlockEntity blockEntity, ItemStack destroyedWith, CallbackInfo ci) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        int depth = DestroyageMgr.depthAt(level, pos);
        boolean correctDepth = depth == 0;
        boolean holdingKeybind = MultiblockMineChecks.isHoldingKeybind(serverPlayer);
        boolean hasEnchantment = MultiblockMineChecks.enchantment(level, destroyedWith);
        boolean isOre = MultiblockMineChecks.doItAnywaysBecauseThisIsAnOre(state, serverPlayer);
        boolean startMultiblockMine = correctDepth &&
                (
                    (
                        holdingKeybind && hasEnchantment
                    )
                    ||
                    (
                        isOre && hasEnchantment
                    )
                );
        QSMultiblockMiner.LOGGER.info("Correct depth: {}, depth was {}; Holding Keybind: {}; Has enchantment: {}; Is Ore: {}; Start?: {}", correctDepth, depth, holdingKeybind, hasEnchantment, isOre, startMultiblockMine);

        if (startMultiblockMine) {
            int nextDepth = depth + 1;
            for (Direction dir : Direction.values()) {
                BlockPos next = pos.relative(dir);
                if (level.getBlockState(next).is(state.getBlock())) {
                    DestroyageMgr.start(serverPlayer, next, nextDepth);
                }
            }
        }
    }
}