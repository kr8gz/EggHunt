package io.github.kr8gz.egghunt.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun createTables() = transaction {
    SchemaUtils.create(Players, Eggs, FoundEggs)
}

object Players : UUIDTable("players") {
    val name = varchar("name", 16)
}

class Player(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Player>(Players)

    var uuid by Players.id
    var name by Players.name
}

object Eggs : IntIdTable("eggs") {
    val world = text("world")
    val x = integer("x")
    val y = integer("y")
    val z = integer("z")
    val placedBy = reference("placed_by", Players.id)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp())
}

class Egg(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Egg>(Eggs)

    var world by Eggs.world
    var x by Eggs.x
    var y by Eggs.y
    var z by Eggs.z
    var placedBy by Player referencedOn Eggs.placedBy
    var createdAt by Eggs.createdAt
}

object FoundEggs : Table("found_eggs") {
    val egg = reference("egg", Eggs.id, onDelete = ReferenceOption.CASCADE)
    val player = reference("player", Players.id)
    val foundAt = timestamp("found_at").defaultExpression(CurrentTimestamp())
    override val primaryKey = PrimaryKey(egg, player)
}
