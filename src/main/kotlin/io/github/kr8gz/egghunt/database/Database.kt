package io.github.kr8gz.egghunt.database

import io.github.kr8gz.egghunt.EggHunt
import io.github.kr8gz.egghunt.database.Table.*
import io.github.kr8gz.egghunt.database.Table.Eggs as EggPositions
import io.github.kr8gz.egghunt.world.EggPosition
import net.minecraft.entity.player.PlayerEntity
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

object Database {
    private lateinit var DATABASE_URL: String

    private val connection by object {
        var connection: Connection? = null

        operator fun getValue(thisRef: Any?, property: Any?): Connection {
            return connection?.takeUnless { it.isClosed }
                ?: DriverManager.getConnection(DATABASE_URL).also { connection = it }
        }
    }

    fun initialize(savePath: Path) {
        DATABASE_URL = "jdbc:sqlite:${savePath.resolve("${EggHunt.MOD_NAME}.db").toFile()}?foreign_keys=on"
        connection.createStatement().use(Table::createAll)
    }

    object Eggs {
        private val cachedPositions = with(EggPositions) {
            connection.prepareStatement("SELECT $world, $x, $y, $z FROM $EggPositions").executeQuery().use { rs ->
                generateSequence {
                    rs.takeIf { it.next() }?.run { EggPosition(getString(world), getInt(x), getInt(y), getInt(z)) }
                }.toHashSet()
            }
        }

        /** @return the ID of the new egg */
        fun create(pos: EggPosition): Int = with(EggPositions) {
            connection.prepareStatement("INSERT INTO $EggPositions ($world, $x, $y, $z) VALUES (?, ?, ?, ?)").run {
                with(pos) {
                    setString(1, world)
                    setInt(2, x)
                    setInt(3, y)
                    setInt(4, z)
                    executeUpdate()
                    cachedPositions.add(this)
                }
                generatedKeys.run { next(); getInt(1) }
            }
        }

        @JvmStatic
        fun isAtPosition(pos: EggPosition) = pos in cachedPositions

        /** @return whether an egg was deleted at the position */
        fun delete(pos: EggPosition): Boolean = with(EggPositions) {
            connection.prepareStatement("DELETE FROM $EggPositions WHERE $world = ? AND $x = ? AND $y = ? AND $z = ?").run {
                with(pos) {
                    setString(1, world)
                    setInt(2, x)
                    setInt(3, y)
                    setInt(4, z)
                }

                (executeUpdate() > 0).also { eggDeleted ->
                    if (eggDeleted) cachedPositions.remove(pos)
                }
            }
        }

        /** @return the number of deleted eggs */
        fun deleteAll(): Int = connection.prepareStatement("DELETE FROM $EggPositions")
            .executeUpdate()
            .also { cachedPositions.clear() }

        fun totalCount() = cachedPositions.size
    }

    data class Player(val player: PlayerEntity) {
        fun updateName(): Unit = with(Players) {
            connection.prepareStatement("INSERT INTO $Players ($uuid, $name) VALUES (?, ?) ON CONFLICT($uuid) DO UPDATE SET $name = ?").run {
                setString(1, player.uuidAsString)
                val playerName = player.name.string
                setString(2, playerName)
                setString(3, playerName)
                executeUpdate()
            }
        }

        fun foundEggCount(): Int = with(FoundEggs) {
            val count = "count"
            connection.prepareStatement("SELECT COUNT(*) $count FROM $FoundEggs WHERE $playerUUID = ?").run {
                setString(1, player.uuidAsString)
                executeQuery().use { rs ->
                    rs.getInt(count)
                }
            }
        }

        /** @return whether the player found a new egg */
        fun tryFindEgg(pos: EggPosition): Boolean {
            val foundEggId = with(EggPositions) {
                connection.prepareStatement("SELECT $id FROM $EggPositions WHERE $world = ? AND $x = ? AND $y = ? AND $z = ?").run {
                    with(pos) {
                        setString(1, world)
                        setInt(2, x)
                        setInt(3, y)
                        setInt(4, z)
                    }

                    executeQuery().use { rs ->
                        if (rs.next()) rs.getInt(id) else return false
                    }
                }
            }

            return with(FoundEggs) {
                connection.prepareStatement("INSERT OR IGNORE INTO $FoundEggs ($eggId, $playerUUID) VALUES (?, ?)").run {
                    setInt(1, foundEggId)
                    setString(2, player.uuidAsString)
                    executeUpdate() > 0
                }
            }
        }

        fun resetFoundEggs(): Unit = with(FoundEggs) {
            connection.prepareStatement("DELETE FROM $FoundEggs WHERE $playerUUID = ?").run {
                setString(1, player.uuidAsString)
                executeUpdate()
            }
        }
    }

    data class LeaderboardEntry(val rank: Int, val playerName: String, val eggsFound: Int)

    fun getLeaderboard(): List<LeaderboardEntry> {
        val count = "count"
        val rank = "rank"

        val query = """
            SELECT
                ${Players.name},
                COUNT(*) $count,
                RANK() OVER (ORDER BY COUNT(*) DESC) $rank
            FROM $FoundEggs
            JOIN $Players
                ON $Players.${Players.uuid} = $FoundEggs.${FoundEggs.playerUUID}
            GROUP BY ${Players.name}
            ORDER BY $count DESC
        """

        return connection.prepareStatement(query).executeQuery().use { rs ->
            generateSequence {
                rs.takeIf { it.next() }?.run { LeaderboardEntry(getInt(rank), getString(Players.name), getInt(count)) }
            }.toList()
        }
    }
}
