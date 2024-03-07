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
        Tables.create()
    }

    object Eggs {
        /** @return the ID of the new egg */
        fun create(globalPos: GlobalPos, placedByPlayer: PlayerEntity): Int = transaction {
            val inserted = Tables.Eggs.insert {
                it[world] = globalPos.dimension.value.toString()
                with(globalPos) {
                    it[x] = pos.x
                    it[y] = pos.y
                    it[z] = pos.z
                }
                it[placedBy] = placedByPlayer.uuid
            }
            inserted[Tables.Eggs.id].value
        }

        fun isAtPosition(pos: GlobalPos): Boolean = transaction {
            Tables.Eggs.selectAll().where { pos.queryHasEgg() }.count() > 0
        }

        /** @return whether an egg was deleted at the position */
        fun delete(pos: GlobalPos): Boolean = transaction {
            Tables.Eggs.deleteWhere { pos.queryHasEgg() } > 0
        }

        /** @return the number of deleted eggs */
        fun deleteAll(): Int = transaction { Tables.Eggs.deleteAll() }

        fun totalCount(): Long = transaction { Tables.Eggs.selectAll().count() }
    }

    data class Player(val player: PlayerEntity) {
        fun updateName(): Unit = transaction {
            Tables.Players.upsert {
                it[id] = player.uuid
                it[name] = player.name.string
            }
        }

        fun foundEggCount(): Long = transaction {
            Tables.FoundEggs.selectAll().where { Tables.FoundEggs.playerUUID eq player.uuid }.count()
        }

        /** @return whether the egg has not already been found by the player */
        fun checkFoundEgg(pos: GlobalPos): Boolean = transaction {
            Tables.FoundEggs.insertIgnore {
                it[eggId] = Tables.Eggs.selectAll().where { pos.queryHasEgg() }.first()[Tables.Eggs.id]
                it[playerUUID] = player.uuid
            }.insertedCount > 0
        }

        fun resetFoundEggs(): Unit = transaction {
            Tables.FoundEggs.deleteWhere { playerUUID eq player.uuid }
        }
    }

    data class LeaderboardEntry(val rank: Long, val playerName: String, val eggsFound: Long)

    fun getLeaderboard(): List<LeaderboardEntry> = transaction {
        val count = Tables.FoundEggs.eggId.count()
        val name = Tables.Players.name
        val rank = Rank().over().orderBy(count, order = SortOrder.DESC)

        (Tables.FoundEggs innerJoin Tables.Players)
            .select(rank, name, count)
            .groupBy(Tables.Players.name)
            .orderBy(count)
            .toList()
            .map { row -> LeaderboardEntry(row[rank], row[name], row[count]) }
    }
}
