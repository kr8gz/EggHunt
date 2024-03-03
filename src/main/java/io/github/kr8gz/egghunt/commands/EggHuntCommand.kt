package io.github.kr8gz.egghunt.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.world.EggPlacer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

class EggHuntCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            dispatcher.register(CommandManager.literal(EggHunt.MOD_ID)
                .then(CommandManager.literal("place").executes { context ->
                    @Suppress("UsePropertyAccessSyntax") // playerOrThrow isn't like a property lol
                    val player = context.source.getPlayerOrThrow()
                    EggPlacer.giveEggItem(player)
                    Command.SINGLE_SUCCESS
                })
                .then(CommandManager.literal("leaderboard").executes { context ->
                    val leaderboard = StringBuilder()
                    Database.getLeaderboard().forEachIndexed { index, pair ->
                        leaderboard.append("$index. ${pair.first} - ${pair.second}")
                    }
                    context.source.sendFeedback({ Text.literal(leaderboard.toString()) }, false)
                    Command.SINGLE_SUCCESS
                })
            )
        }
    }
}
