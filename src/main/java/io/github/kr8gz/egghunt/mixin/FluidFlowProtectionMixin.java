package io.github.kr8gz.egghunt.mixin;

import io.github.kr8gz.egghunt.database.Database;
import io.github.kr8gz.egghunt.world.EggPosition;
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

@Mixin(FlowableFluid.class)
public abstract class FluidFlowProtectionMixin {
    @Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
    private void canFill(BlockView blockView, BlockPos pos, BlockState blockState, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
        if (blockView instanceof World world && Database.Eggs.isAtPosition(new EggPosition(world, pos))) {
            cir.setReturnValue(false);
        }
    }
}
