package io.github.kr8gz.egghunt

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.UUID

// idk how exactly you want to implement it
object Database {
    fun getLeaderboard(): List<Pair<UUID, Int>> {
        return foundEggs
            .map { it.second }
            .toSet()
            .map { it to getEggCount(it) }
            .sortedByDescending { it.second }
    }
}

// temporary for testing
var nextId = 0
val eggLocations = HashMap<Int, BlockPos>()
val foundEggs = ArrayList<Pair<Int, UUID>>()

data class Egg(val id: Int) {
    companion object Factory {
        fun create(pos: BlockPos) {
            eggLocations[++nextId] = pos
        }

        @JvmStatic
        fun findAtLocation(pos: BlockPos): Egg? {
            return eggLocations.entries.find { it.value == pos }?.let { Egg(it.key) }
        }
    }

    fun remove() {
        // remove all entries with this egg's id from all tables
        eggLocations.remove(id)
        foundEggs.removeIf { it.first == id }
    }
}

fun PlayerEntity.foundEgg(egg: Egg): Boolean {
    // try to create an entry with the egg id and the player uuid, and return whether the egg has not been found by the player with the uuid already
    val pair = egg.id to uuid
    if (foundEggs.contains(pair)) return false
    foundEggs.add(pair)
    return true
}

fun getEggCount(uuid: UUID): Int {
    return foundEggs.count { it.second == uuid }
}
