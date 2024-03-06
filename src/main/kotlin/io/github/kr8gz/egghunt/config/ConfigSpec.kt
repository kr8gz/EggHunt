package io.github.kr8gz.egghunt.config

data class Config(
    val defaultPermissionLevel: Int,
    val sendCommandFeedback: Boolean,

    val onEggFound: OnEggFound,
    val onFoundAll: OnFoundAll,
    val eggPlacement: EggPlacement,
)

data class OnEggFound(
    val spawnFireworks: Boolean,
    val commands: List<String>,
)

data class OnFoundAll(
    val commands: List<String>,
)

data class EggPlacement(
    val textures: List<String>,
)
