package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.commands.EggHuntCommand
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EggHunt : ModInitializer {
    const val MOD_ID = "egghunt"
    val MOD_NAME = this::class.simpleName

    val LOGGER: Logger = LogManager.getLogger()

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            EggHuntCommand.register(dispatcher)
        }

        EggPlacer.registerPlayerBlockBreakListener()
    }
}

fun PlayerEntity.eggHuntMessage(message: Text) {
    sendMessage(Text.literal("[${EggHunt.MOD_NAME}] ").append(message))
}
