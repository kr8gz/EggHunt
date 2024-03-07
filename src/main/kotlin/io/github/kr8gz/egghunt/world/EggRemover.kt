package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import io.github.kr8gz.egghunt.plus
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World

object EggRemover {
    /**
     * Since all eggs are removed in [checkForEggRemoval] before the listener in
     * [registerBlockBreakListeners] is fired, we need a variable to track
     * the last egg that was removed. Using this, we can always remove the egg
     * first and send a message later when it was actually broken by a player.
     **/
    private var lastRemovedEggPos: GlobalPos? = null

    @JvmStatic
    fun checkForEggRemoval(world: World, pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        val blockChanged = oldBlock.block != newBlock.block
        lastRemovedEggPos = (pos within world).takeIf { blockChanged && Database.Eggs.delete(it) }
    }

    fun registerBlockBreakListeners() {
        PlayerBlockBreakEvents.BEFORE.register { world, player, pos, _, _ ->
            if (!Database.Eggs.isAtPosition(pos within world)) return@register true

            Permissions.check(player, EggHunt.Permissions.REMOVE, config.defaultPermissionLevel).also { hasPermission ->
                if (!hasPermission) player.sendMessage(
                    EggHunt.MESSAGE_PREFIX.append(Formatting.RED + "You don't have permission to remove eggs!")
                )
            }
        }

        PlayerBlockBreakEvents.AFTER.register { world, player, pos, _, _ ->
            if (lastRemovedEggPos == pos within world) {
                player.sendMessage(EggHunt.MESSAGE_PREFIX.append(Formatting.RED + "Egg removed!"))
            }
        }
    }
}
