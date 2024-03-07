package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.database.Database.checkFoundEgg
import io.github.kr8gz.egghunt.database.Database.getEggCount
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.item.FireworkRocketItem
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

object EggFindDetector {
    fun registerBlockClickListeners() {
        UseBlockCallback.EVENT.register { player, world, hand, result ->
            if (hand == Hand.MAIN_HAND) {
                eggFindListener(player, world, result.blockPos)
            }
            ActionResult.PASS
        }

        AttackBlockCallback.EVENT.register { player, world, _, pos, _ ->
            eggFindListener(player, world, pos)
            ActionResult.PASS
        }
    }

    private fun eggFindListener(player: PlayerEntity, world: World, pos: BlockPos) {
        if (player.isSpectator || !Database.isEggAtPos(pos within world)) return

        if (!player.checkFoundEgg(pos within world)) {
            player.sendMessage(EggHunt.MESSAGE_PREFIX.append("${Formatting.RED}You already found this egg!"))
            return
        }

        player.sendMessage(EggHunt.MESSAGE_PREFIX.append("${Formatting.GREEN}You found an egg!"))

        with(config.onEggFound) {
            if (spawnFireworks) spawnFirework(world, pos)
            runCommands(world, player, commands)
        }

        if (Database.getTotalEggCount() == player.getEggCount()) {
            runCommands(world, player, config.onFoundAll.commands)
        }
    }

    private fun spawnFirework(world: World, pos: BlockPos) {
        val fireworkItem = Items.FIREWORK_ROCKET.defaultStack.apply {
            getOrCreateSubNbt(FireworkRocketItem.FIREWORKS_KEY).apply {
                putByte(FireworkRocketItem.FLIGHT_KEY, 0)
                put(FireworkRocketItem.EXPLOSIONS_KEY, NbtList().apply {
                    add(NbtCompound().apply {
                        putByte(FireworkRocketItem.TYPE_KEY, FireworkRocketItem.Type.BURST.id.toByte())
                        putIntArray(FireworkRocketItem.COLORS_KEY, intArrayOf(DyeColor.entries.random().fireworkColor))
                    })
                })
            }
        }

        world.spawnEntity(with(pos.toCenterPos()) {
            FireworkRocketEntity(world, x, y, z, fireworkItem)
        })
    }

    private fun runCommands(world: World, player: PlayerEntity, commands: List<String>) {
        world.server?.run {
            commands.forEach {
                val source = commandSource.takeIf { config.sendCommandFeedback } ?: commandSource.withSilent()
                commandManager.executeWithPrefix(source, "execute as ${player.uuid} run $it")
            }
        }
    }
}
