package se.ade.mc.skyblock.datastore

import org.jetbrains.exposed.sql.Table

object PlayerStatusTable: Table() {
	val uuid = varchar("uuid", 36).uniqueIndex()
	override val primaryKey: PrimaryKey = PrimaryKey(uuid)

	val health = double("health")
	val food = integer("food")
	val air = integer("air")
}