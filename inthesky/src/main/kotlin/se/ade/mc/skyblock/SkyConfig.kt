package se.ade.mc.skyblock

import kotlinx.serialization.Serializable
import se.ade.mc.skyblock.structuremaps.StructureMapDrawData

@Serializable
data class SkyConfig(
	val debug: Boolean = false,
	val trader: TraderConfig = TraderConfig(),
)

@Serializable
data class TraderConfig(
	val shardIngredientKeys: List<String> = listOf()
)