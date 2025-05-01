package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack

fun creeperDropsBlazePowderRule(e: EntityDeathEvent) {
	if(e.entity.type != EntityType.CREEPER)
		return

	if(e.entity.killer == null)
		return

	if(e.entity.fireTicks < 1)
		return

	val modified = e.drops.map {
		if(it.type == Material.GUNPOWDER) {
			ItemStack(Material.BLAZE_POWDER)
		} else {
			it
		}
	}
	e.drops.clear()
	e.drops.addAll(modified)
}