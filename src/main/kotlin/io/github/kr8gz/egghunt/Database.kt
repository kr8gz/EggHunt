package io.github.kr8gz.egghunt

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.math.BlockPos
import org.sqlite.SQLiteException
import java.io.IOException
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import kotlin.io.path.createFile
import kotlin.io.path.exists

const val schemaEggTable = """
CREATE TABLE IF NOT EXISTS egg (
    id INTEGER PRIMARY KEY,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    placed_by_uuid TEXT(36) NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
)
"""

const val schemaFoundEggsTable = """
CREATE TABLE IF NOT EXISTS found_egg (
    egg_id INT NOT NULL,
    player_uuid TEXT(36) NOT NULL,
    found_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (egg_id, player_uuid),
    FOREIGN KEY (egg_id) REFERENCES egg(id) ON DELETE CASCADE,
    FOREIGN KEY (player_uuid) REFERENCES player(uuid) ON DELETE CASCADE
)
"""

const val schemaPlayerTable = """
CREATE TABLE IF NOT EXISTS player (
    uuid TEXT(36) PRIMARY KEY,
    name TEXT(100) NOT NULL
)
"""

object Database {
    private val DATABASE_PATH = FabricLoader.getInstance().gameDir.resolve("${EggHunt.MOD_NAME}.db")

    private val connection = getNewConnection()
        get() = field.takeUnless { it.isClosed } ?: getNewConnection()

    private fun getNewConnection() = DriverManager.getConnection("jdbc:sqlite:$DATABASE_PATH")

    fun initialize() {
        if (!DATABASE_PATH.exists()) {
            try {
                EggHunt.LOGGER.info("Creating database file at ${DATABASE_PATH.toAbsolutePath()}")
                DATABASE_PATH.createFile()
            } catch (e: IOException) {
                EggHunt.LOGGER.error("Unable to create database file", e)
            }
        }
        setupTables()
    }

    private fun setupTables() {
        try {
            connection.createStatement().run {
                executeUpdate(schemaEggTable)
                executeUpdate(schemaPlayerTable)
                executeUpdate(schemaFoundEggsTable)

                // enables foreign keys including references and cascading deletion
                execute("PRAGMA foreign_keys = ON")

                close()
            }
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to create database schema", e)
        }
    }

    /**
     * @return the ID of the new egg, or `null` if it could not be created
     **/
    fun createEggAtPos(pos: BlockPos, placedByUUID: UUID): Int? {
        return try {
            connection.prepareStatement("INSERT INTO egg (x, y, z, placed_by_uuid) VALUES (?, ?, ?, ?)")
                .run {
                    setInt(1, pos.x)
                    setInt(2, pos.y)
                    setInt(3, pos.z)
                    setString(4, placedByUUID.toString())
                    executeUpdate()
                }

            connection.prepareStatement("SELECT last_insert_rowid() AS last_id")
                .executeQuery()
                .getInt("last_id")
        }
        catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to insert egg into database", e)
            null
        }
    }

    /**
     * @return whether there is an egg at the position,
     * or `false` if the query could not be executed
     **/
    fun isEggAtPos(pos: BlockPos): Boolean = with(pos) {
        try {
            connection.prepareStatement("SELECT 1 FROM egg WHERE x = ? AND y = ? AND z = ?")
                .run {
                    setInt(1, x)
                    setInt(2, y)
                    setInt(3, z)
                    executeQuery()
                }
                .use(ResultSet::next)
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to determine if there is an egg at position $x $y $z", e)
            false
        }
    }

    /**
     * @return whether an egg was deleted at the position
     **/
    fun deleteEggAtPos(pos: BlockPos): Boolean = with(pos) {
        try {
            connection.prepareStatement("DELETE FROM egg WHERE x = ? AND y = ? AND z = ?")
                .run {
                    setInt(1, x)
                    setInt(2, y)
                    setInt(3, z)
                    executeUpdate() > 0
                }
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to delete egg at position $x $y $z", e)
            false
        }
    }

    data class LeaderboardEntry(val rank: Int, val playerName: String, val eggsFound: Int)

    fun getLeaderboard(): List<LeaderboardEntry>? {
        val query = """
            SELECT
                name,
                COUNT(*) found_count,
                RANK() OVER (ORDER BY COUNT(*) DESC) rank
            FROM found_egg fe
            JOIN player p ON p.uuid = fe.player_uuid
            GROUP BY name
            ORDER BY found_count DESC
        """

        return try {
            connection.prepareStatement(query).executeQuery().use { rs ->
                generateSequence {
                    rs.takeIf { it.next() }?.run { LeaderboardEntry(
                        getInt("rank"),
                        getString("name"),
                        getInt("found_count"),
                    ) }
                }.toList()
            }
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to execute leaderboard query", e)
            null
        }
    }

    /**
     * @return the total number of eggs in the database, or `null` if a database operation failed
     **/
    fun getTotalEggCount(): Int? {
        return try {
            connection.prepareStatement("SELECT COUNT(*) AS total_eggs FROM egg")
                .executeQuery()
                .getInt("total_eggs")
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to get total egg count", e)
            null
        }
    }

    fun updatePlayerName(player: PlayerEntity): Unit = with(player) {
        val name = name.literalString
        try {
            connection.prepareStatement("INSERT INTO player (uuid, name) VALUES(?, ?) ON CONFLICT(uuid) DO UPDATE SET name=?")
                .run {
                    setString(1, uuid.toString())
                    setString(2, name)
                    setString(3, name)
                    executeUpdate()
                }
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to update player name of $name ($uuid)", e)
        }
    }

    /**
     * @return the total number of eggs the player has found, or `null` if a database operation failed
     **/
    fun PlayerEntity.getEggCount(): Int? {
        return try {
            connection.prepareStatement("SELECT COUNT(*) AS found_count FROM found_egg WHERE player_uuid = ?")
                .apply { setString(1, uuid.toString()) }
                .executeQuery()
                .getInt("found_count")
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to get egg count for player $uuid", e)
            null
        }
    }

    /**
     * @return whether the egg has not already been found by the player,
     * or `null` if a database operation failed
     **/
    fun PlayerEntity.checkFoundEgg(pos: BlockPos): Boolean? {
        val eggId = with(pos) {
            try {
                connection.prepareStatement("SELECT id FROM egg WHERE x = ? AND y = ? AND z = ?")
                    .run {
                        setInt(1, x)
                        setInt(2, y)
                        setInt(3, z)
                        executeQuery()
                    }
                    .use { rs -> if (rs.next()) rs.getInt("id") else return false }
            } catch (e: SQLException) {
                EggHunt.LOGGER.error("Unable to get egg ID at position $x $y $z", e)
                return null
            }
        }

        return try {
            connection.prepareStatement("INSERT OR IGNORE INTO found_egg (egg_id, player_uuid) VALUES (?, ?)")
                .run {
                    setInt(1, eggId)
                    setString(2, uuid.toString())
                    executeUpdate() > 0
                }
        } catch (e: SQLException) {
            EggHunt.LOGGER.error("Unable to register $uuid finding egg $eggId", e)
            null
        }
    }
}
