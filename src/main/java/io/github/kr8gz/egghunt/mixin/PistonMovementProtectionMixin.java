package io.github.kr8gz.egghunt.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kr8gz.egghunt.database.Database;
import io.github.kr8gz.egghunt.world.EggPosition;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
public abstract class PistonMovementProtectionMixin {
    @Shadow @Final private World world;

    @Shadow @Final private BlockPos posTo;

    @Inject(method = "calculatePush",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.BEFORE),
            cancellable = true)
    private void calculatePush(CallbackInfoReturnable<Boolean> cir, @Local BlockState blockState) {
        if (Database.Eggs.isAtPosition(new EggPosition(world, posTo))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tryMove", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
    private void tryMove(BlockPos ignored, Direction dir, CallbackInfoReturnable<Boolean> cir, @Local(ordinal = 1) BlockPos pos) {
        if (Database.Eggs.isAtPosition(new EggPosition(world, posTo))) {
            cir.setReturnValue(false);
        }
    }
}
