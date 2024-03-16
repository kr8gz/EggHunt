package io.github.kr8gz.egghunt

import io.github.kr8gz.egghunt.database.Database
import io.github.kr8gz.egghunt.world.EggPosition
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

fun PlayerEntity.inDatabase() = Database.Player(this)

infix fun BlockPos.within(world: World) = EggPosition(world, this)

operator fun Formatting.plus(text: String): MutableText = Text.literal(text).formatted(this)

operator fun MutableText.plus(text: Text): MutableText = this.append(text)
