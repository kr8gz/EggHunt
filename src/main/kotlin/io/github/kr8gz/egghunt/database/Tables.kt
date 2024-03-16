package io.github.kr8gz.egghunt.database

import java.sql.Statement

sealed class Table {
    protected abstract val tableName: String
    override fun toString() = tableName

    protected abstract val schema: List<String>

    companion object {
        fun createAll(statement: Statement) {
            // for all `object` declarations inheriting `Table`
            Table::class.sealedSubclasses.mapNotNull { it.objectInstance }.forEach {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS ${it.tableName} (${it.schema.joinToString()})")
            }
        }
    }

    object Players : Table() {
        override val tableName = "players"

        const val uuid = "uuid"
        const val name = "name"

        override val schema = listOf(
            "$uuid BINARY(16) NOT NULL PRIMARY KEY",
            "$name VARCHAR(16) NOT NULL",
        )
    }

    object Eggs : Table() {
        override val tableName = "eggs"

        const val id = "id"
        const val world = "world"
        const val x = "x"
        const val y = "y"
        const val z = "z"

        override val schema = listOf(
            "$id INTEGER PRIMARY KEY",
            "$world TEXT NOT NULL",
            "$x INT NOT NULL",
            "$y INT NOT NULL",
            "$z INT NOT NULL",
        )
    }

    object FoundEggs : Table() {
        override val tableName = "found_eggs"

        const val eggId = "egg"
        const val playerUUID = "player"

        override val schema = listOf(
            "$eggId INT NOT NULL",
            "$playerUUID BINARY(16) NOT NULL",
            "PRIMARY KEY ($eggId, $playerUUID)",
            "FOREIGN KEY ($eggId) REFERENCES $Eggs(${Eggs.id}) ON DELETE CASCADE",
            "FOREIGN KEY ($playerUUID) REFERENCES $Players(${Players.uuid}) ON DELETE CASCADE",
        )
    }
}
