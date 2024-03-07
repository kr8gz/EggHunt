package io.github.kr8gz.egghunt.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.config.config
import io.github.kr8gz.egghunt.database.inDatabase
import io.github.kr8gz.egghunt.world.EggPlacer
import me.lucko.fabric.api.permissions.v0.Permissions
import net.minecraft.command.argument.EntityArgumentType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

typealias ServerCommandContext = CommandContext<ServerCommandSource>

object EggHuntCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(literal(EggHunt.MOD_NAME.lowercase())
            .then(literal("progress").executes { context ->
                context.source.sendFeedback({ DisplayCommands.getProgressMessage(context) }, false)
                Command.SINGLE_SUCCESS
            })
            .then(literal("leaderboard").executes { context ->
                context.source.sendFeedback({ DisplayCommands.getLeaderboardMessage(context) }, false)
                Command.SINGLE_SUCCESS
            })
            .then(literal("place")
                .requires(Permissions.require(EggHunt.Permissions.PLACE, config.defaultPermissionLevel))
                .executes { context ->
                    giveEggItem(context)
                    Command.SINGLE_SUCCESS
                }
            )
            .then(literal("reset")
                .requires(Permissions.require(EggHunt.Permissions.RESET, config.defaultPermissionLevel))
                .then(literal("eggs")
                    .requires(Permissions.require(EggHunt.Permissions.RESET_EGGS, config.defaultPermissionLevel))
                    .executes(::removeAllEggs)
                )
                .then(literal("player")
                    .requires(Permissions.require(EggHunt.Permissions.RESET_PLAYER, config.defaultPermissionLevel))
                    .then(argument("players", EntityArgumentType.players())
                        .executes { context ->
                            resetPlayerProgress(context, EntityArgumentType.getPlayers(context, "players"))
                        }
                    )
                )
            )
        )
    }

    private fun giveEggItem(context: ServerCommandContext) {
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
    }

    private fun removeAllEggs(context: ServerCommandContext): Int {
        return Database.Eggs.deleteAll().also { count ->
            context.source.sendFeedback({ EggHunt.MESSAGE_PREFIX.append("Removed $count eggs") }, true)
        }
    }

    private fun resetPlayerProgress(context: ServerCommandContext, players: Collection<PlayerEntity>): Int {
        return players.onEach { it.inDatabase().resetFoundEggs() }.size.also { count ->
            context.source.sendFeedback({
                EggHunt.MESSAGE_PREFIX.append("Reset found eggs for $count player${count.pluralSuffix("s")}")
            }, true)
        }
    }
}
