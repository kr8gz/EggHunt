package io.github.kr8gz.egghunt.mixin;

import io.github.kr8gz.egghunt.database.Database;
import io.github.kr8gz.egghunt.world.EggPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BucketItem.class)
public abstract class FluidPlacementProtectionMixin {
    @Inject(method = "placeFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;breakBlock(Lnet/minecraft/util/math/BlockPos;Z)Z", shift = At.Shift.BEFORE), cancellable = true)
    private void placeFluid(PlayerEntity player, World world, BlockPos pos, BlockHitResult hitResult, CallbackInfoReturnable<Boolean> cir) {
        if (Database.Eggs.isAtPosition(new EggPosition(world, pos))) {
            cir.setReturnValue(false);
        }
    }
}
