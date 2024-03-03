package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.command.EggHuntCommand
import io.github.kr8gz.egghunt.config.initializeConfig
import io.github.kr8gz.egghunt.world.EggFindDetector
import io.github.kr8gz.egghunt.world.EggRemover
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EggHunt : ModInitializer {
    const val MOD_ID = "egghunt"
    val MOD_NAME = this::class.simpleName!!

    val LOGGER: Logger = LogManager.getLogger()

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            EggHuntCommand.register(dispatcher)
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            Database.updatePlayerName(handler.player)
        }

        EggRemover.registerPlayerBlockBreakListener()
        EggFindDetector.registerBlockClickListeners()

        Database.initialize()
        initializeConfig()
    }
}

fun PlayerEntity.eggHuntMessage(message: String, formatting: Formatting) {
    val prefix = Text.literal("[${EggHunt.MOD_NAME}] ")
    val formattedMessage = Text.literal(message).formatted(formatting)
    sendMessage(prefix.append(formattedMessage))
}
