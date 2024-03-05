package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.EggHunt
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EggRemover {
    /**
     * Since all eggs are removed in [checkForEggRemoval] before the listener in
     * [registerPlayerBlockBreakListener] is fired, we need a variable to track
     * the last egg that was removed. Using this, we can always remove the egg
     * first and send a message later when it was actually broken by a player.
     **/
    private var lastRemovedEggPos: Pair<World, BlockPos>? = null

    @JvmStatic
    fun checkForEggRemoval(world: World, pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        val blockChanged = oldBlock.block != newBlock.block
        lastRemovedEggPos = (world to pos).takeIf { blockChanged && Database.deleteEggAtPos(world, pos) }
    }

    fun registerPlayerBlockBreakListener() {
        PlayerBlockBreakEvents.AFTER.register { world, player, pos, _, _ ->
            if (lastRemovedEggPos == world to pos) {
                player.sendMessage(EggHunt.MESSAGE_PREFIX.append("${Formatting.RED}Egg removed!"))
            }
        }
    }
}
