package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.command.EggHuntCommand
import io.github.kr8gz.egghunt.world.EggFindDetector
import io.github.kr8gz.egghunt.world.EggRemover
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EggHunt : ModInitializer {
    val MOD_NAME = this::class.simpleName!!
    val LOGGER: Logger = LogManager.getLogger()

    override fun onInitialize() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            EggHuntCommand.register(dispatcher)
        }

        EggRemover.registerPlayerBlockBreakListener()
        EggFindDetector.registerBlockClickListeners()
    }
}

fun PlayerEntity.eggHuntMessage(message: String, formatting: Formatting) {
    val prefix = Text.literal("[${EggHunt.MOD_NAME}] ")
    val formattedMessage = Text.literal(message).formatted(formatting)
    sendMessage(prefix.append(formattedMessage))
}
