package io.github.kr8gz.egghunt

import net.minecraft.util.math.BlockPos
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*


const val schemaEggTable: String = """
CREATE TABLE IF NOT EXISTS egg (
    id INTEGER PRIMARY KEY,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    placed_by_uuid TEXT(36) NOT NULL,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
"""
const val schemaFoundEggsTable: String = """
CREATE TABLE IF NOT EXISTS found_egg (
    egg_id INT NOT NULL,
    player_uuid INT NOT NULL,
    found_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (egg_id, player_uuid),
    FOREIGN KEY (egg_id) REFERENCES egg(id) ON DELETE CASCADE
);
"""
object Database {
    private val LOGGER: Logger = LogManager.getLogger()
    private var connection: Connection? = null
    private var dbPath: Path? = null

    fun initialize(gameDir: Path) {
        dbPath = gameDir.resolve("EggHunt.db")

        val dbFile = dbPath!!.toFile()

        if (!dbFile.exists()) {
            try {
                LOGGER.info("Creating EggHunt database file " + dbPath!!.toAbsolutePath())
                dbFile.createNewFile()
            } catch (e: IOException) {
                LOGGER.error("Unable to create database file", e)
            }
        }

        val conn = getConnection()
        setupTables(conn)
    }
    private fun getConnection(): Connection {
        if (connection?.isClosed == false)
            return connection!!

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        } catch(e: SQLException) {
            LOGGER.error("SQLite failed to initialize", e)
        } catch (e: ClassNotFoundException) {
            LOGGER.error("Unable to find SQLite library.", e)
        }

        return connection!!
    }

    private fun setupTables(conn: Connection) {
        try {
            val statement = conn.createStatement()
            statement.executeUpdate(schemaEggTable)
            statement.executeUpdate(schemaFoundEggsTable)
            statement.close()
        } catch (e: SQLException) {
            LOGGER.error("Unable to create schema for EggHunt", e)
        }
    }

    fun insertEgg(pos: BlockPos, playerUUID: UUID): Int? {
        try {
            val conn = getConnection()
            val ps = conn.prepareStatement("INSERT INTO egg (x, y, z, placed_by_uuid) VALUES (?, ?, ?, ?)")
            ps.setInt(1, pos.x)
            ps.setInt(2, pos.y)
            ps.setInt(3, pos.z)
            ps.setString(4, playerUUID.toString())

            ps.executeUpdate()

            val rs = conn.prepareStatement("SELECT last_insert_rowid() AS last_id;").executeQuery()
            return rs.getInt("last_id")
        } catch (e: SQLException) {
            LOGGER.error("Unable to insert egg into database", e)
        }
        return null
    }

    fun findEgg(pos: BlockPos, playerUUID: UUID): Boolean {
        // Check for eggs
        val conn = getConnection()
        var ps = conn.prepareStatement("SELECT id, ( SELECT COUNT(*) FROM found_egg WHERE egg_id = id AND player_uuid = ? LIMIT 1) AS found_count FROM egg WHERE x = ? AND y = ? AND z = ?;")
        ps.setString(1, playerUUID.toString())
        ps.setInt(2, pos.x)
        ps.setInt(3, pos.y)
        ps.setInt(4, pos.z)
        val rs: ResultSet?
        try {
            rs = ps.executeQuery()
        } catch (e: SQLException) {
            LOGGER.error("Unable to query for found eggs", e)
            return false
        }

        val eggId = rs.getInt("id")
        val foundCount = rs.getInt("found_count")

        if (foundCount > 0)
            return false

        if (eggId == 0) {
            LOGGER.info("Egg 0 at $pos")
        }

        ps = conn.prepareStatement("INSERT INTO found_egg (egg_id, player_uuid) VALUES (?, ?)")
        ps.setInt(1, eggId)
        ps.setString(2, playerUUID.toString())
        try {
            ps.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error("Unable to insert found_egg for $eggId $playerUUID", e)
            return false
        }

        return true
    }

    fun deleteEggAtPos(pos: BlockPos) {
        val conn = getConnection()
        val ps = conn.prepareStatement("DELETE FROM egg WHERE x = ? AND y = ? AND z = ?")
        ps.setInt(1, pos.x)
        ps.setInt(2, pos.y)
        ps.setInt(3, pos.z)

        try {
            ps.executeUpdate()
        } catch (e: SQLException) {
            LOGGER.error("Unable to delete egg at pos: $pos", e)
        }
    }
    fun getLeaderboard(): List<Pair<UUID, Int>> {
        val conn = getConnection()
        val ps = conn.prepareStatement("SELECT player_uuid, COUNT(*) found_count FROM found_egg GROUP BY player_uuid")
        val rs: ResultSet?
        try {
            rs = ps.executeQuery()
        } catch (e: SQLException) {
            LOGGER.error("Unable to run leaderboard query", e)
            return ArrayList<Pair<UUID, Int>>()
        }

        return rs.use {
            generateSequence {
                if (rs.next()) Pair<UUID, Int>(UUID.fromString(rs.getString("player_uuid")), rs.getInt("found_count")) else null
            }.toList()
        }
    }
}