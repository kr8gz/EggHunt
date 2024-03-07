package io.github.kr8gz.egghunt.database

import net.minecraft.entity.player.PlayerEntity

fun PlayerEntity.inDatabase(): Database.Player = Database.Player(this)
