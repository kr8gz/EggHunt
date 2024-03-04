package io.github.kr8gz.egghunt.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.kr8gz.egghunt.Database
import io.github.kr8gz.egghunt.Database.getEggCount
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.world.EggPlacer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.text.DecimalFormat

@Suppress("UsePropertyAccessSyntax") // playerOrThrow isn't like a property lol
object EggHuntCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        dispatcher.register(CommandManager.literal(EggHunt.MOD_NAME.lowercase())
            .then(CommandManager.literal("leaderboard").executes(::displayLeaderboard))
            .then(CommandManager.literal("place").executes(::giveEggItem))
            .then(CommandManager.literal("progress").executes(::displayPlayerProgress))
        )
    }

    private fun displayLeaderboard(context: CommandContext<ServerCommandSource>): Int {
        with(context.source) {
            sendFeedback({
                // FIXME this is ugly af
                val text = EggHunt.MESSAGE_PREFIX.append(Text.literal("Leaderboard").formatted(Formatting.YELLOW))
                val executorPlayerName = player?.name?.literalString
                val leaderboard = Database.getLeaderboard()!!
                val topEntries = leaderboard.take(9).toMutableList()
                leaderboard.find { it.playerName == executorPlayerName }?.let {
                    if (it !in topEntries) topEntries[topEntries.lastIndex] = it
                }
                topEntries.forEach {
                    text.run {
                        append("\n${it.rank}. ")
                        append(Text.literal(it.playerName).formatted(if (it.playerName == executorPlayerName) Formatting.GREEN else Formatting.RED))
                        append(Text.literal(" found").formatted(Formatting.GRAY))
                        append(" %,d".format(it.eggsFound))
                        append(Text.literal(" eggs").formatted(Formatting.GRAY))
                    }
                }
                text
            }, false)
        }
        return Command.SINGLE_SUCCESS
    }

    private fun giveEggItem(context: CommandContext<ServerCommandSource>): Int {
        with(context.source) {
            val player = getPlayerOrThrow()
            val item = EggPlacer.getEggItem()

            // copy so it can be displayed in the command feedback
            item.copy().takeUnless { player.giveItemStack(it) }?.let {
                player.dropItem(it, false)?.apply {
                    resetPickupDelay()
                    setOwner(player.getUuid())
                }
            }

            sendFeedback({
                Text.translatable("commands.give.success.single", 1, item.toHoverableText(), player.displayName)
            }, true)
        }
        return Command.SINGLE_SUCCESS
    }

    private fun displayPlayerProgress(context: CommandContext<ServerCommandSource>): Int {
        with(context.source) {
            sendFeedback({
                // FIXME this is ugly af too
                val playerFound = getPlayerOrThrow().getEggCount()!!
                val totalEggs = Database.getTotalEggCount()!!
                val text = EggHunt.MESSAGE_PREFIX
                text.run {
                    (playerFound / totalEggs.toFloat() * 100).takeUnless { it.isNaN() }?.let {
                        val percentage = DecimalFormat("#.##").format(it)
                        val percentageColor = when {
                            it in 0f..25f -> Formatting.RED
                            it in 25f..50f -> Formatting.GOLD
                            it in 50f..75f -> Formatting.YELLOW
                            playerFound == totalEggs -> Formatting.AQUA
                            else -> Formatting.GREEN
                        }
                        append(Text.literal("You found ").formatted(Formatting.GRAY))
                        append("$playerFound")
                        append(Text.literal(" out of ").formatted(Formatting.GRAY))
                        append("$totalEggs")
                        append(Text.literal(" eggs (").formatted(Formatting.GRAY))
                        append(Text.literal("$percentage%").formatted(percentageColor))
                        append(Text.literal(")").formatted(Formatting.GRAY))
                    } ?: run {
                        append(Text.literal("There are no eggs to be found... yet!").formatted(Formatting.RED))
                    }
                }
            }, false)
        }
        return Command.SINGLE_SUCCESS
    }
}
