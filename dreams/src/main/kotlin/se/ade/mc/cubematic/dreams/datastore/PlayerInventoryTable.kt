package se.ade.mc.cubematic.dreams.datastore

import org.jetbrains.exposed.v1.core.Table

object PlayerInventoryTable : Table() {
	val uuid = varchar("uuid", 36).uniqueIndex()
	val inventory = binary("inventory")
	val armor = binary("armor")
	override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}