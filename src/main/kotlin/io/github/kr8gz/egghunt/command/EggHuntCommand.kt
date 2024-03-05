package io.github.kr8gz.egghunt.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.world.EggPlacer
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text

@Suppress("UsePropertyAccessSyntax") // playerOrThrow isn't like a property lol
object EggHuntCommand {
    fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
        val teleportCommand = literal("teleport").build() // id int argument

        dispatcher.register(literal(EggHunt.MOD_NAME.lowercase())
            // TODO permissions
            // All players can use
            .then(literal("progress").executes(::displayPlayerProgress))
            .then(literal("leaderboard").executes(::displayLeaderboard))

            // Egg placement permission
            .then(literal("place").executes(::giveEggItem))

            // Either teleport or egg removal permission
            .then(literal("list"))

            // Teleport permission
            .then(teleportCommand)
            .then(literal("tp").redirect(teleportCommand))

            // Egg removal permission
            .then(literal("remove")) // id int argument | literal all

            // Egg reset permission
            .then(literal("reset")) // literal player, player selector argument | literal all
        )
    }

    private fun giveEggItem(context: CommandContext<ServerCommandSource>): Int {
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
        return Command.SINGLE_SUCCESS
    }
}
