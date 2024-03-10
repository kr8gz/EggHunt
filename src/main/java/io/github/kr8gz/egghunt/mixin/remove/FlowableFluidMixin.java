package io.github.kr8gz.egghunt.mixin.remove;

import io.github.kr8gz.egghunt.world.EggRemover;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Prevents fluid flow from breaking eggs */
@Mixin(FlowableFluid.class)
public abstract class FlowableFluidMixin {
    @Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
    private void canFill(BlockView blockView, BlockPos pos, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (blockView instanceof World world && EggRemover.isEggAt(world, pos)) {
            cir.setReturnValue(false);
        }
    }
}
