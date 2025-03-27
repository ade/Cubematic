package se.ade.mc.cubematic.portals

import kotlinx.serialization.Serializable

@Serializable
data class PortalsConfig(
	val debug: Boolean = false,

	/** Allow players to break and pick up end frame blocks with at least an iron pickaxe */
	val breakableEndFrames: Boolean = false,
)