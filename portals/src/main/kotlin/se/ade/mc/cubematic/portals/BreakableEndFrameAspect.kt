package se.ade.mc.cubematic.portals

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.extensions.PickTier
import se.ade.mc.cubematic.extensions.adjacentHorizontal
import se.ade.mc.cubematic.extensions.isPickOfAtleast

/**
 * Allows the player to (instant) break END_PORTAL_FRAME blocks with at least an iron pickaxe,
 * also disabling the portal, if any, connected to the broken block
 */
class BreakableEndFrameAspect(private val plugin: CubematicPortalsPlugin): Aspect(plugin) {
	private val listener = EndFrameAspectListener()

	override fun enable() {
		addListener(listener)
	}

	override fun disable() {}
}

class EndFrameAspectListener: Listener {
	@EventHandler
	fun onEvent(event: PlayerInteractEvent) {
		if(event.hand != EquipmentSlot.HAND)
			return

		val itemInHand = event.item
			?: return

		val clickedBlock = event.clickedBlock
			?: return

		if (event.action === Action.LEFT_CLICK_BLOCK &&
				itemInHand.type isPickOfAtleast PickTier.IRON &&
				clickedBlock.type == Material.END_PORTAL_FRAME &&
				event.getPlayer().gameMode == GameMode.SURVIVAL
		) {
			event.setCancelled(true)
			clickedBlock.type = Material.AIR
			clickedBlock.adjacentHorizontal().forEach {
				floodUnFillEndPortal(it)
			}
			clickedBlock.world.dropItemNaturally(
				clickedBlock.location.toCenterLocation(),
				ItemStack(Material.END_PORTAL_FRAME)
			)
		}
	}

	/**
	 * Flood fills connected END_PORTAL blocks with AIR starting from the given block
	 */
	private fun floodUnFillEndPortal(startBlock: Block) {
		if (startBlock.type != Material.END_PORTAL) {
			return
		}

		val queue = ArrayDeque<Block>()
		val visited = mutableSetOf<Block>()

		queue.add(startBlock)
		visited.add(startBlock)

		while (queue.isNotEmpty()) {
			val current = queue.removeFirst()

			if (current.type == Material.END_PORTAL) {
				current.type = Material.AIR

				// Add all unvisited adjacent END_PORTAL blocks to the queue
				for (adjacent in current.adjacentHorizontal()) {
					if (adjacent.type == Material.END_PORTAL && adjacent !in visited) {
						queue.add(adjacent)
						visited.add(adjacent)
					}
				}
			}
		}
	}

}