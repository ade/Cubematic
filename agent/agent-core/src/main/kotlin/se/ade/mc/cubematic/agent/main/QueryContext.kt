package se.ade.mc.cubematic.agent.main

import kotlinx.serialization.Serializable
import org.bukkit.GameMode
import org.bukkit.Material

@Serializable
data class QueryContext(
	val playerName: String,
	val playerLevel: Int,
	val health: Int,
	val foodLevel: Int,
	val location: LocationContext,
	val time: String,
	val nearbyEntities: List<String>,
	val inventoryItems: List<InventoryItem>,
	val gameMode: GameMode,
) {
	@Serializable
	data class InventoryItem(
		val type: Material,
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

