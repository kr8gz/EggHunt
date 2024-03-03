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
        context.source.sendFeedback({
            Text.literal(buildString {
                Database.getLeaderboard().forEachIndexed { index, pair ->
                    val (name, count) = pair
                    append("${index + 1}. $name - $count")
                }
            })
        }, false)
        return Command.SINGLE_SUCCESS
    }

    private fun giveEggItem(context: CommandContext<ServerCommandSource>): Int {
        with(context.source) {
            val player = getPlayerOrThrow()
            val item = EggPlacer.getEggItem()

            item.copy().let { // copy so it can be displayed in the command feedback
                if (!player.giveItemStack(it)) { // if the item could not be inserted
                    player.dropItem(it, false)?.apply {
                        resetPickupDelay()
                        setOwner(player.getUuid())
                    }
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
                Text.literal("${getPlayerOrThrow().getEggCount()} / ${Database.getTotalEggCount()}")
            }, false)
        }
        return Command.SINGLE_SUCCESS
    }
}
