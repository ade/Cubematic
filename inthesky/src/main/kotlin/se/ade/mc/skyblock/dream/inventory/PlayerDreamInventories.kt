package se.ade.mc.skyblock.dream.inventory

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.datastore.SkyDb
import java.util.logging.Logger

class PlayerDreamInventories(
	private val db: SkyDb,
	private val logger: Logger
) {
	fun stash(player: Player) {
		// Save the inventory to a file
		val inventory = ItemStack.serializeItemsAsBytes(player.inventory.contents)
		val armor = ItemStack.serializeItemsAsBytes(player.inventory.armorContents)

		db.playerInventoryStash(
			uuid = player.identity().uuid(),
			inventory = inventory,
			armor = armor
		)

		// Clear the inventory
		player.inventory.clear()
		player.inventory.armorContents = emptyArray()
		player.updateInventory()
	}

	fun pop(player: Player) {
		val stashed = db.playerInventoryPop(player.identity().uuid())
			?: run {
				logger.warning("Player ${player.name} is not hibernating")
				return
			}

		player.inventory.contents = ItemStack.deserializeItemsFromBytes(stashed.inventory)
		player.inventory.armorContents = ItemStack.deserializeItemsFromBytes(stashed.armor)
		player.updateInventory()
	}
}