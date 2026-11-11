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
import net.quedoom.multiblock_miner.networking.MultimineState;
import net.quedoom.multiblock_miner.things.DestroyageMgr;
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
        if (depth == 0 && !MultimineState.isActive(serverPlayer)) return;

        int nextDepth = depth + 1;
        for (Direction dir : Direction.values()) {
            BlockPos next = pos.relative(dir);
            if (level.getBlockState(next).is(state.getBlock())) {
                DestroyageMgr.start(serverPlayer, next, nextDepth);
            }
        }
    }
}