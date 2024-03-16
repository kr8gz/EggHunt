package io.github.kr8gz.egghunt.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.kr8gz.egghunt.database.Database;
import io.github.kr8gz.egghunt.world.EggPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public abstract class ExplosionProtectionMixin {
    @Shadow @Final private World world;

    @ModifyExpressionValue(
            method = "collectBlocksAndDamageEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/ExplosionBehavior;canDestroyBlock(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)Z")
    )
    @SuppressWarnings("unused")
    private boolean canDestroyBlock(boolean original, @Local BlockPos pos) {
        return original && !Database.Eggs.isAtPosition(new EggPosition(world, pos));
    }
}
