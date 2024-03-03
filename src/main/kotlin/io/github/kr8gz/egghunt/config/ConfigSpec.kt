package io.github.kr8gz.egghunt.config

data class Config(
    val onEggFound: OnEggFound,
)

data class OnEggFound(
    val spawnFireworks: Boolean,
    val commands: List<String>,
)
