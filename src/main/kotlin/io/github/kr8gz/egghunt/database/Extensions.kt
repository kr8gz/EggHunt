package io.github.kr8gz.egghunt.database

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.GlobalPos
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

fun PlayerEntity.inDatabase(): Database.Player = Database.Player(this)

fun GlobalPos.queryHasEgg() = with(Tables.Eggs) {
    (world eq dimension.value.toString())
        .and(x eq pos.x)
        .and(y eq pos.y)
        .and(z eq pos.z)
}
