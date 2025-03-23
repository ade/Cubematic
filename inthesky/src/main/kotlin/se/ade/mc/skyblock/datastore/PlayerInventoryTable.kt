package se.ade.mc.skyblock.datastore

import org.jetbrains.exposed.sql.Table

object PlayerInventoryTable : Table() {
	val uuid = varchar("uuid", 36).uniqueIndex()
	val inventory = binary("inventory")
	val armor = binary("armor")
	override val primaryKey: PrimaryKey = PrimaryKey(uuid)
}