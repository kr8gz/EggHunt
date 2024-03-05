package io.github.kr8gz.egghunt.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.world.EggPlacer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

object EggHuntCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val progressCommand = literal("progress").executes { context ->
            context.source.sendFeedback({ DisplayCommands.getProgressMessage(context) }, false)
            Command.SINGLE_SUCCESS
        }

        val leaderboardCommand = literal("leaderboard").executes { context ->
            context.source.sendFeedback({ DisplayCommands.getLeaderboardMessage(context) }, false)
            Command.SINGLE_SUCCESS
        }

        val placeCommand = literal("place")
            .executes { context ->
                @Suppress("UsePropertyAccessSyntax")
                val player = context.source.getPlayerOrThrow()
                val item = EggPlacer.randomEggItem()

                // copy so it can be displayed in the command feedback
                item.copy().takeUnless { player.giveItemStack(it) }?.let {
                    player.dropItem(it, false)?.apply {
                        resetPickupDelay()
                        setOwner(player.uuid)
                    }
                }

                context.source.sendFeedback({
                    Text.translatable("commands.give.success.single", 1, item.toHoverableText(), player.displayName)
                }, true)
                Command.SINGLE_SUCCESS
            }

        val listCommand = literal("list").build()

        val teleportCommand = literal("teleport").build() // id int argument

        val removeCommand = literal("remove").build() // id int argument | literal all

        val resetCommand = literal("reset").build() // literal player, player selector argument | literal all

        dispatcher.register(literal(EggHunt.MOD_NAME.lowercase())
            // TODO permissions
            // All players can use
            .then(progressCommand)
            .then(leaderboardCommand)

            // Egg placement permission
            .then(placeCommand)

            // Either teleport or egg removal permission
            .then(listCommand)

            // Teleport permission
            .then(teleportCommand)
            .then(literal("tp").redirect(teleportCommand))

            // Egg removal permission
            .then(removeCommand)

            // Egg reset permission
            .then(resetCommand)
        )
    }
}
