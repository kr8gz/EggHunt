package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.commands.EggHuntCommand
import io.github.kr8gz.egghunt.config.initializeConfig
import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.world.EggFindDetector
import io.github.kr8gz.egghunt.world.EggRemover
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.WorldSavePath
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EggHunt : DedicatedServerModInitializer {
    const val MOD_ID = "egghunt"
    val MOD_NAME = this::class.simpleName!!

    val MESSAGE_PREFIX: MutableText = Text.literal("[$MOD_NAME] ")
        get() = field.copy() // otherwise all modifications will apply to the same text object

    object Permissions {
        const val PLACE = "$MOD_ID.place"
        const val REMOVE = "$MOD_ID.remove"
        const val RESET = "$MOD_ID.reset"
        const val RESET_EGGS = "$RESET.eggs"
        const val RESET_PLAYER = "$RESET.player"
    }

    val LOGGER: Logger = LogManager.getLogger()

    override fun onInitializeServer() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            EggHuntCommand.register(dispatcher)
        }

        EggRemover.registerBlockBreakListeners()
        EggFindDetector.registerBlockClickListeners()

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            Database.initialize(server.getSavePath(WorldSavePath.ROOT))
        }

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            handler.player.inDatabase().updateName()
        }

        initializeConfig()
    }
}
