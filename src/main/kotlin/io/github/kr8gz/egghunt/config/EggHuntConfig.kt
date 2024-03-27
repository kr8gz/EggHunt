package io.github.kr8gz.egghunt.config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.watch.ReloadableConfig
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import io.github.kr8gz.egghunt.EggHunt
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val TEMPLATE_PATH = FabricLoader.getInstance()
    .getModContainer(EggHunt.MOD_ID).get()
    .findPath("config.toml").get()

private val RUNTIME_PATH = FabricLoader.getInstance()
    .configDir.resolve("${EggHunt.MOD_NAME}.toml")

private fun recreateRuntimeConfig() {
    EggHunt.LOGGER.info("Recreating config file from template")
    Files.copy(TEMPLATE_PATH, RUNTIME_PATH, StandardCopyOption.REPLACE_EXISTING)
}

private lateinit var configReloader: ReloadableConfig<Config>

// separate initialization function to control exactly when the config is initialized
// so that it doesn't have to initialize when the server is already running
fun initializeConfig() {
    val loader = ConfigLoader { addPathSource(RUNTIME_PATH) }
    if (!Files.exists(RUNTIME_PATH) || loader.loadConfig<Config>().isInvalid()) {
        recreateRuntimeConfig()
    }

    configReloader = ReloadableConfig(loader, Config::class).apply {
        addWatcher(FileWatcher(RUNTIME_PATH.parent.toString()))
        withErrorHandler { exception ->
            if (!Files.exists(RUNTIME_PATH)) {
                recreateRuntimeConfig()
            } else {
                EggHunt.LOGGER.error(exception)
            }
        }
        subscribe { EggHunt.LOGGER.info("Reloaded config") }
    }
}

val config: Config
    get() = configReloader.getLatest()
