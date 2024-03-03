package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.Egg
import io.github.kr8gz.egghunt.eggHuntMessage
import io.github.kr8gz.egghunt.foundEgg
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos

object EggClickDetector {
    fun registerBlockClickListeners() {
        UseBlockCallback.EVENT.register { player, _, hand, result ->
            if (hand == Hand.MAIN_HAND) {
                eggFindListener(player, result.blockPos)
            }
            ActionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, _, _, pos, _ ->
            eggFindListener(player, pos)
            ActionResult.PASS
        }
    }

    private fun eggFindListener(player: PlayerEntity, pos: BlockPos) {
        Egg.findAtLocation(pos)?.run {
            if (player.foundEgg(this)) {
                player.eggHuntMessage("You found an egg!", Formatting.GREEN)
            } else {
                player.eggHuntMessage("You already found this egg!", Formatting.RED)
            }
        }
    }
}
