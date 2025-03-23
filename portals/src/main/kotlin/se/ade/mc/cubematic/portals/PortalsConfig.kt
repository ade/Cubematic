package se.ade.mc.cubematic.portals

import kotlinx.serialization.Serializable

@Serializable
data class PortalsConfig(
	val debug: Boolean = false,
)