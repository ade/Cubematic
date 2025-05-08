package se.ade.mc.skyblock

import kotlinx.serialization.Serializable

@Serializable
data class SkyConfig(
	val debug: Boolean = false,
	val trader: TraderConfig = TraderConfig(),
)

@Serializable
data class TraderConfig(
	val shardIngredientKeys: List<String> = listOf()
)