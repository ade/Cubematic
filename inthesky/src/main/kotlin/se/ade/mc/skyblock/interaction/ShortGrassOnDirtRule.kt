package se.ade.mc.skyblock.interaction

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import se.ade.mc.cubematic.effects.boneMealGrowEffect
import se.ade.mc.cubematic.extensions.spend

/**
 * Allows the player to turn DIRT blocks into GRASS_BLOCK blocks by
 * right-clicking it with short grass, if the player is not sneaking,
 * making grass blocks an obtainable resource (without first having it).
 * @return true if the event was consumed, false otherwise
 */
fun shortGrassOnDirtInteraction(e: PlayerInteractEvent): Boolean {
	val action = e.action

	if(action != Action.RIGHT_CLICK_BLOCK) return false
	val clickedBlock = e.clickedBlock ?: return false
	if(clickedBlock.type != Material.DIRT) return false
	val item = e.item ?: return false
	if(item.type != Material.SHORT_GRASS) return false
	val hand = e.hand ?: return false

	if(e.player.isSneaking) {
		// Allow vanilla short grass placement
		return false
	}

	// All checks passed, place grass.
	e.isCancelled = true
	clickedBlock.type = Material.GRASS_BLOCK
	boneMealGrowEffect(clickedBlock)

	if(e.player.gameMode == GameMode.CREATIVE) {
		return true
	} else {
		e.player.inventory.setItem(hand, e.player.inventory.getItem(hand).spend(1))
		return true
	}
}