package se.ade.mc.skyblock

import kotlinx.serialization.Serializable
import se.ade.mc.skyblock.structuremaps.StructureOutlineData

@Serializable
data class SkyConfig(
	val debug: Boolean = false,
	val trader: TraderConfig = TraderConfig(),
	val structureMapData: Map<Int, StructureOutlineData> = mapOf()
)

@Serializable
data class TraderConfig(
	val shardIngredientKeys: List<String> = listOf()
)