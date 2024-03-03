package io.github.kr8gz.egghunt

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import java.util.UUID

// idk how exactly you want to implement it
object Database {
    fun getLeaderboard(): List<Pair<UUID, Int>> = foundEggs
            .groupingBy { it.second }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }

    fun getTotalEggCount() = eggLocations.size
}

// temporary for testing
private var nextId = 0
    get() = ++field

private val eggLocations = HashMap<Int, BlockPos>()
private val foundEggs = HashSet<Pair<Int, UUID>>()

data class Egg(val id: Int) {
    companion object {
        fun create(pos: BlockPos) {
            eggLocations[nextId] = pos
        }

        fun findAtLocation(pos: BlockPos) = eggLocations.entries
            .find { it.value == pos }
            ?.let { Egg(it.key) }
    }

    fun remove() {
        // remove all entries with this egg's id from all tables
        eggLocations.remove(id)
        foundEggs.removeIf { it.first == id }
    }
}

// try to create an entry with the egg id and the player uuid, and return whether the egg has not been found by the player with the uuid already
fun PlayerEntity.foundEgg(egg: Egg) = foundEggs.add(egg.id to uuid)

fun PlayerEntity.getEggCount() = foundEggs.count { it.second == uuid }
