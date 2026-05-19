package se.ade.mc.skyblock.datastore

import org.jetbrains.exposed.v1.core.Table

object StructureMapTable: Table("structure_maps") {
	val mapId = integer("map_id").uniqueIndex()
	val structureType = varchar("structure_type", 64)
}