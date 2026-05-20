package se.ade.mc.cubematic.core.agent.main

import kotlinx.serialization.Serializable

@Serializable
data class QueryContext(
	val serverInfo: ServerInfo,
	val playerName: String,
	val playerLevel: Int,
	val health: Int,
	val foodLevel: Int,
	val location: LocationContext,
	val time: String,
	val nearbyEntities: List<String>,
	val inventoryItems: List<InventoryItem>,
	val gameMode: String,
	val chatHistory: List<Pair<String, String>> = emptyList()
) {
	@Serializable
	data class InventoryItem(
		val type: String,
		val quantity: Int
	)

	@Serializable
	data class LocationContext(
		val worldName: String,
		val x: Int,
		val y: Int,
		val z: Int
	)
}

@Serializable
data class ServerInfo(
	val version: String
)
