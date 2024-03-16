package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.plus
import io.github.kr8gz.egghunt.within
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.text.Text
import net.minecraft.util.Formatting

object EggRemover {
    private var playerEggBreakPos: EggPosition? = null

    @JvmStatic
    fun shouldPreserveBlock(pos: EggPosition, oldState: BlockState, newState: BlockState): Boolean {
        val blockChanged = !oldState.isOf(newState.block)
        return blockChanged && playerEggBreakPos != pos && Database.Eggs.isAtPosition(pos)
    }

    fun registerBlockBreakListeners() {
        PlayerBlockBreakEvents.BEFORE.register { world, player, pos, _, _ ->
            val eggPos = pos within world
            if (!Database.Eggs.isAtPosition(eggPos)) return@register true

            Permissions.check(player, EggHunt.Permissions.REMOVE, config.defaultPermissionLevel).also { hasPermission ->
                if (!hasPermission) player.sendMessage(
                    EggHunt.MESSAGE_PREFIX + Text.translatable("egghunt.egg.removed.forbidden").formatted(Formatting.RED)
                ) else {
                    playerEggBreakPos = eggPos
                }
            }
        }

        PlayerBlockBreakEvents.AFTER.register { world, player, pos, _, _ ->
            if (playerEggBreakPos?.takeIf { Database.Eggs.delete(it) } == pos within world) {
                player.sendMessage(EggHunt.MESSAGE_PREFIX + Text.translatable("egghunt.egg.removed").formatted(Formatting.RED))
            }
            playerEggBreakPos = null
        }
    }
}
