package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Egg
import io.github.kr8gz.egghunt.eggHuntMessage
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

object EggRemover {
    /**
     * Since all eggs are removed in [checkForEggRemoval] before the listener in
     * [registerPlayerBlockBreakListener] is fired, we need a variable to track
     * the last egg that was removed. Using this, we can always remove the egg
     * first and send a message later when it was actually broken by a player.
     **/
    private var lastRemovedEggPos: BlockPos? = null

    @JvmStatic
    fun checkForEggRemoval(pos: BlockPos, oldBlock: BlockState, newBlock: BlockState) {
        lastRemovedEggPos = null
        if (oldBlock.block != newBlock.block) {
            Egg.findAtLocation(pos)?.run {
                remove()
                lastRemovedEggPos = pos
            }
        }
    }

    fun registerPlayerBlockBreakListener() {
        PlayerBlockBreakEvents.AFTER.register { _, player, pos, _, _ ->
            if (lastRemovedEggPos == pos) {
                player.eggHuntMessage("Egg removed!", Formatting.RED)
            }
        }
    }
}
