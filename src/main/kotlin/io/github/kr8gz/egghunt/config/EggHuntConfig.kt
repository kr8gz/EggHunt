package io.github.kr8gz.egghunt.config

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.watch.ReloadableConfig
import com.sksamuel.hoplite.watch.watchers.FileWatcher
import io.github.kr8gz.egghunt.EggHunt
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val CONFIG_FILE_NAME = "${EggHunt.MOD_NAME}.toml"

private val RUNTIME_CONFIG_PATH = FabricLoader.getInstance()
    .configDir.resolve(CONFIG_FILE_NAME)

private val TEMPLATE_CONFIG_PATH = FabricLoader.getInstance()
    .getModContainer(EggHunt.MOD_ID).get()
    .findPath(CONFIG_FILE_NAME).get()

private fun recreateRuntimeConfig() {
    Files.copy(TEMPLATE_CONFIG_PATH, RUNTIME_CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING)
}

private lateinit var configReloader: ReloadableConfig<Config>

// separate initialization function to control exactly when the config is initialized
// so that it doesn't have to initialize when the server is already running
fun initializeConfig() {
    val loader = ConfigLoader { addPathSource(RUNTIME_CONFIG_PATH) }
    if (!Files.exists(RUNTIME_CONFIG_PATH) || loader.loadConfig<Config>().isInvalid()) {
        recreateRuntimeConfig()
    }

    configReloader = ReloadableConfig(loader, Config::class)
        .addWatcher(FileWatcher(RUNTIME_CONFIG_PATH.parent.toString()))
        .withErrorHandler { exception ->
            if (!Files.exists(RUNTIME_CONFIG_PATH)) {
                recreateRuntimeConfig()
            } else {
                EggHunt.LOGGER.error(exception)
            }
        }
}

val config: Config
    get() = configReloader.getLatest()
