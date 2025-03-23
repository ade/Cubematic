package se.ade.mc.skyblock

import kotlinx.serialization.Serializable

@Serializable
data class SkyConfig(
	val debug: Boolean = false,
)