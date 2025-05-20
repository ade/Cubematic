package se.ade.mc.skyblock.trader

import org.bukkit.Material
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.generator.structure.Structure
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.structuremaps.createStructureMap

fun traderMapsRule(event: CreatureSpawnEvent, plugin: CubematicSkyPlugin) {
	val trader = event.entity as? WanderingTrader
		?: return

	val possibleTypes = listOf(
		Structure.MONUMENT to "Monument",
		Structure.SWAMP_HUT to "Swamp Hut",
		Structure.PILLAGER_OUTPOST to "Pillager Outpost",
	)

	val rnd = possibleTypes.random()
	val type = rnd.first
	val title = rnd.second

	plugin.logger.info { "Attempt to find: $title" }

	val map = createStructureMap(
		loc = event.location,
		plugin = plugin,
		structure = type,
		title = title
	) ?: run {
		plugin.logger.warning { "Failed to create map for $title" }
		return
	}

	trader.recipes = trader.recipes + listOf(
		MerchantRecipe(map, 1).also {
			it.ingredients = listOf(
				ItemStack(Material.EMERALD, 5),
			)
			it.maxUses = 1
		}
	)
}