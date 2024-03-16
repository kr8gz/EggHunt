package io.github.kr8gz.egghunt.mixin;

import io.github.kr8gz.egghunt.world.EggPosition;
import io.github.kr8gz.egghunt.world.EggRemover;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class SetBlockStateProtectionMixin {
    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void setBlockState(BlockPos pos, BlockState newState, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        var eggPos = new EggPosition((World) (Object) this, pos);
        if (EggRemover.shouldPreserveBlock(eggPos, this.getBlockState(pos), newState)) {
            cir.setReturnValue(false);
        }
    }
}
