package se.ade.mc.cubematic.dreams.datastore

import org.jetbrains.exposed.v1.core.Table

object PlayerLocationTable: Table() {
	val uuid = varchar("uuid", 36).uniqueIndex()
	override val primaryKey: PrimaryKey = PrimaryKey(uuid)

	val x = double("x")
	val y = double("y")
	val z = double("z")
	val yaw = float("yaw")
	val pitch = float("pitch")
}