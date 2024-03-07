package io.github.kr8gz.egghunt.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

fun createTables() = transaction {
    SchemaUtils.create(Players, Eggs, FoundEggs)
}

object Players : UUIDTable("players") {
    val name = varchar("name", 16)
}

object Eggs : IntIdTable("eggs") {
    val world = text("world")
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val placedBy = reference("placed_by", Players.id)
}

object FoundEggs : Table("found_eggs") {
    val egg = reference("egg", Eggs.id, onDelete = ReferenceOption.CASCADE)
    val player = reference("player", Players.id)
    override val primaryKey = PrimaryKey(egg, player)
}
