package io.github.kr8gz.egghunt.database

import io.github.kr8gz.egghunt.EggHunt
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.GlobalPos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Database {
    private val DATABASE_PATH = FabricLoader.getInstance().gameDir.resolve("${EggHunt.MOD_NAME}.db")

    fun initialize() {
        Database.connect("jdbc:sqlite:$DATABASE_PATH?foreign_keys=on")
        createTables()
    }

    /** @return the ID of the new egg */
    fun createEgg(globalPos: GlobalPos, placedByPlayer: PlayerEntity): Int = transaction {
        Egg.new {
            world = globalPos.dimension.value.toString()
            with(globalPos) {
                x = pos.x
                y = pos.y
                z = pos.z
            }
            placedBy = Player.findById(placedByPlayer.uuid)!!
        }.id.value
    }

    fun isEggAtPos(pos: GlobalPos): Boolean = transaction {
        Egg.find { eggAtPosition(pos) }.firstOrNull() != null
    }

    /** @return whether an egg was deleted at the position */
    fun deleteEgg(pos: GlobalPos): Boolean = transaction {
        Eggs.deleteWhere { eggAtPosition(pos) } > 0
    }

    fun deleteAllEggs(): Int = transaction { Eggs.deleteAll() }

    fun getTotalEggCount(): Long = transaction { Eggs.selectAll().count() }

    private fun eggAtPosition(globalPos: GlobalPos) = with(globalPos) {
        (Eggs.world eq dimension.value.toString())
            .and(Eggs.x eq pos.x)
            .and(Eggs.y eq pos.y)
            .and(Eggs.z eq pos.z)
    }

    data class LeaderboardEntry(val rank: Long, val playerName: String, val eggsFound: Long)

    fun getLeaderboard(): List<LeaderboardEntry> = transaction {
        val count = FoundEggs.egg.count()
        val rank = Rank().over().orderBy(count, order = SortOrder.DESC)

        (FoundEggs innerJoin Players)
            .select(rank, Players.name, count)
            .groupBy(Players.name)
            .orderBy(count)
            .toList()
            .map { row -> LeaderboardEntry(row[rank], row[Players.name], row[count]) }
    }

    fun updatePlayerName(player: PlayerEntity): Unit = transaction {
        Players.upsert {
            it[id] = player.uuid
            it[name] = player.name.string
        }
    }

    fun PlayerEntity.getEggCount(): Long = transaction {
        FoundEggs.selectAll().where { FoundEggs.player eq uuid }.count()
    }

    /** @return whether the egg has not already been found by the player */
    fun PlayerEntity.checkFoundEgg(pos: GlobalPos): Boolean = transaction {
        FoundEggs.insertIgnore {
            it[egg] = Egg.find { eggAtPosition(pos) }.first().id
            it[player] = uuid
        }.insertedCount > 0
    }

    fun PlayerEntity.resetFoundEggs(): Unit = transaction {
        FoundEggs.deleteWhere { player eq uuid }
    }
}
