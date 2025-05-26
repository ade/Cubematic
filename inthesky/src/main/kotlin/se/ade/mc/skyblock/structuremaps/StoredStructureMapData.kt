package se.ade.mc.skyblock.structuremaps

import kotlinx.serialization.Serializable

@Serializable
data class StoredStructureMapData(
	/** Map ID as assigned to map */
	val mapId: Int,

	/** Key for the structure type, e.g. "minecraft:fortress" */
	val structureTypeKey: String,
)