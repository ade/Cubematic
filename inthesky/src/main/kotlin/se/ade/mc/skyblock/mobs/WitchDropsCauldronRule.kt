package se.ade.mc.skyblock.mobs

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

fun witchDropsCauldronRule(e: EntityDeathEvent) {
	// When a witch dies, it drops a cauldron, if killed by a player
	if(e.entity.type != EntityType.WITCH)
		return

	// Must be killed by a player
	if(e.entity.killer == null)
		return

	if(Random.nextDouble() <= 0.5)
		e.drops += ItemStack(Material.CAULDRON)
}