package io.github.kr8gz.egghunt.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.EggPlacer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource

class EggHuntCommand {
    companion object {
        fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
            dispatcher.register(CommandManager.literal(EggHunt.MOD_ID)
                .then(CommandManager.literal("place").executes { context ->
                    @Suppress("UsePropertyAccessSyntax") // playerOrThrow isn't like a property lol
                    val player = context.source.getPlayerOrThrow()
                    EggPlacer.giveEggItem(player)
                    Command.SINGLE_SUCCESS
                }))
        }
    }
}
