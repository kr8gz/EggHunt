package io.github.kr8gz.egghunt.database

import io.github.kr8gz.egghunt.EggHunt
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.GlobalPos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Path

object Database {
    private val DATABASE_FILE = "${EggHunt.MOD_NAME}.db"

    fun initialize(savePath: Path) {
        Database.connect("jdbc:sqlite:${savePath.resolve(DATABASE_FILE).toFile()}?foreign_keys=on")
        Tables.create()
    }

    object Eggs {
        private data class EggPosition(val x: Int, val y: Int, val z: Int, val world: String)

        private fun GlobalPos.toEggPosition() = EggPosition(pos.x, pos.y, pos.z, dimension.value.toString())

        private val cachedPositions = transaction {
            Tables.Eggs.selectAll().map {
                with(Tables.Eggs) {
                    EggPosition(it[x], it[y], it[z], it[world])
                }
            }.toHashSet()
        }

        /** @return the ID of the new egg */
        fun create(globalPos: GlobalPos, placedByPlayer: PlayerEntity): Int = transaction {
            val insertedId = Tables.Eggs.insertAndGetId {
                it[world] = globalPos.dimension.value.toString()
                with(globalPos) {
                    it[x] = pos.x
                    it[y] = pos.y
                    it[z] = pos.z
                }
                it[placedBy] = placedByPlayer.uuid
            }
            cachedPositions.add(globalPos.toEggPosition())
            insertedId.value
        }

        fun isAtPosition(globalPos: GlobalPos) = globalPos.toEggPosition() in cachedPositions

        /** @return whether an egg was deleted at the position */
        fun delete(globalPos: GlobalPos): Boolean = transaction {
            Tables.Eggs.deleteWhere { globalPos.queryHasEgg() }.let { deletedCount ->
                cachedPositions.remove(globalPos.toEggPosition())
                deletedCount > 0
            }
        }

        /** @return the number of deleted eggs */
        fun deleteAll(): Int = transaction {
            Tables.Eggs.deleteAll().also { cachedPositions.clear() }
        }

        fun totalCount() = cachedPositions.size
    }

    data class Player(val player: PlayerEntity) {
        fun updateName(): Unit = transaction {
            Tables.Players.upsert {
                it[id] = player.uuid
                it[name] = player.name.string
            }
        }

        fun foundEggCount(): Int = transaction {
            Tables.FoundEggs.selectAll().where { Tables.FoundEggs.playerUUID eq player.uuid }.count().toInt()
        }

        /** @return whether the player found a new egg */
        fun tryFindEgg(pos: GlobalPos): Boolean = transaction {
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

    private fun GlobalPos.queryHasEgg() = with(Tables.Eggs) {
        (world eq dimension.value.toString())
            .and(x eq pos.x)
            .and(y eq pos.y)
            .and(z eq pos.z)
    }
}
