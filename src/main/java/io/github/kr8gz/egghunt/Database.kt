package io.github.kr8gz.egghunt

import net.minecraft.util.math.BlockPos
import java.util.UUID

// idk how exactly you want to implement it
object Database {

}

fun getLeaderboard(): List<Pair<UUID, Int>> {
    TODO("sort descending")
}

data class Egg(val id: Int) {
    companion object Factory {
        fun create(pos: BlockPos) {
            // TODO create an egg entry in the locations table with a new egg id and the block pos
        }

        @JvmStatic
        fun findAtLocation(pos: BlockPos): Egg? {
            // TODO optionally returns an egg if there is one at the block pos
            return Egg(0)
        }
    }

    fun remove() {
        // TODO removes all entries with this egg's id from all tables
        EggHunt.LOGGER.info("pretend this removed something")
    }
}

fun UUID.foundEgg(egg: Egg): Boolean {
    TODO("tries to create an entry with the egg id and the player uuid," +
            "and returns whether the egg has been found by the player with the uuid already")
}

fun UUID.getEggCount(): Int {
    TODO("returns the number of eggs found by the player with the uuid")
}
