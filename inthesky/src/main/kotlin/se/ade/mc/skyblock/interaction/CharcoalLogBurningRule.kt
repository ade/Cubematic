package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.inventory.ItemStack
import se.ade.mc.cubematic.definitions.overworldLogTypes

fun charcoalLogBurnEvent(e: BlockBurnEvent) {
	if(e.block.type in overworldLogTypes) {
		e.block.world.dropItemNaturally(
			e.block.location.toCenterLocation(),
			ItemStack(Material.CHARCOAL)
		)
	}
}