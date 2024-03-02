package io.github.kr8gz.easteregghunt

import net.fabricmc.api.ModInitializer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object EasterEggHunt : ModInitializer {
    val LOGGER: Logger = LogManager.getLogger("EasterEggHunt")

    override fun onInitialize() {
        LOGGER.error("bonjor")
    }
}
