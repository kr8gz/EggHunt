package io.github.kr8gz.egghunt.world

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.GlobalPos
import net.minecraft.world.World

infix fun BlockPos.within(world: World): GlobalPos = GlobalPos.create(world.registryKey, this)
