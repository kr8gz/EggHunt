package io.github.kr8gz.egghunt.world

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import io.github.kr8gz.egghunt.plus
import me.lucko.fabric.api.permissions.v0.Permissions
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World

object EggRemover {
    private var playerEggBreakPos: GlobalPos? = null

    @JvmStatic
    fun isEggAt(world: World, pos: BlockPos) = Database.Eggs.isAtPosition(pos within world)

    @JvmStatic
    fun shouldPreserveBlock(world: World, pos: BlockPos, newState: BlockState): Boolean {
        val blockChanged = !world.getBlockState(pos).isOf(newState.block)
        return blockChanged && playerEggBreakPos != pos within world && isEggAt(world, pos)
    }

    @JvmStatic
    fun updateClient(player: PlayerEntity, world: World, pos: BlockPos) {
        if (player is ServerPlayerEntity) world.getBlockEntity(pos)?.let {
            // TODO update on...
            //  - sticky piston retract
            //  - fluid bucket fill
            //  - break without permission
            player.networkHandler.sendPacket(BlockEntityUpdateS2CPacket.create(it))
        }
    }

    fun registerBlockBreakListeners() {
        PlayerBlockBreakEvents.BEFORE.register { world, player, pos, _, _ ->
            val globalPos = pos within world
            Database.Eggs.isAtPosition(globalPos).let { isEgg ->
                playerEggBreakPos = globalPos.takeIf { isEgg }
                if (!isEgg) return@register true
            }

            Permissions.check(player, EggHunt.Permissions.REMOVE, config.defaultPermissionLevel).also { hasPermission ->
                if (!hasPermission) player.sendMessage(
                    EggHunt.MESSAGE_PREFIX + Text.translatable("egghunt.egg.removed.forbidden").formatted(Formatting.RED)
                )
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
