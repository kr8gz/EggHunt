package io.github.kr8gz.egghunt.world

import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

data class EggPosition(val world: String, val x: Int, val y: Int, val z: Int) {
    constructor(world: World, pos: BlockPos) : this(world.registryKey.value.toString(), pos.x, pos.y, pos.z)
}
