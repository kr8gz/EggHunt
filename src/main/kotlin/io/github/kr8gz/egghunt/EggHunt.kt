package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.command.EggHuntCommand
import io.github.kr8gz.egghunt.config.initializeConfig
import io.github.kr8gz.egghunt.world.EggFindDetector
import io.github.kr8gz.egghunt.world.EggRemover
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EggHunt : ModInitializer {
    const val MOD_ID = "egghunt"
    val MOD_NAME = this::class.simpleName!!
    val MESSAGE_PREFIX: MutableText = Text.literal("[$MOD_NAME] ")
        get() = field.copy() // otherwise all modifications will apply to the same text object

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
