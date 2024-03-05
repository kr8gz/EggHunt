package io.github.kr8gz.egghunt.config

data class Config(
    val onEggFound: OnEggFound,
    val eggPlacement: EggPlacement,
)

data class OnEggFound(
    val spawnFireworks: Boolean,
    val commands: List<String>,
    val sendCommandFeedback: Boolean,
)

data class EggPlacement(
    val textures: List<String>,
)
