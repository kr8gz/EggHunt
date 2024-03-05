package io.github.kr8gz.egghunt.mixin;

import io.github.kr8gz.egghunt.world.EggRemover;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class WorldMixin {
    @Inject(method = "onBlockChanged", at = @At("HEAD"))
    private void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        EggRemover.checkForEggRemoval((ServerWorld) (Object) this, pos, oldBlock, newBlock);
    }
}
