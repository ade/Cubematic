package se.ade.mc.skyblock.trader

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.EntityEquipment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.persistence.PersistentDataType
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin

class TraderFacet(val plugin: CubematicSkyPlugin): Aspect(plugin) {
	private val moddedKey = NamespacedKey(plugin, "trader-modded")
	override fun enable() {
		plugin.server.pluginManager.registerEvents(listener, plugin)
	}

	override fun disable() {

	}

	private fun spawnSpecialTrader(location: Location) {
		location.world.spawnEntity(location, EntityType.WANDERING_TRADER, CreatureSpawnEvent.SpawnReason.CUSTOM) {

		}
	}

	private val listener = object : Listener {
		@EventHandler
		fun onSpawn(event: CreatureSpawnEvent) {
			if(event.entity.type == EntityType.WANDERING_TRADER && event.spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) {
				val trader = (event.entity as WanderingTrader)

				if(trader.persistentDataContainer.get(moddedKey, PersistentDataType.BOOLEAN) == true) {
					// Already modified
					return
				}

				trader.persistentDataContainer.set(moddedKey, PersistentDataType.BOOLEAN, true)

				trader.recipes = trader.recipes + listOf<MerchantRecipe>(
					MerchantRecipe(ItemStack(Material.GRASS_BLOCK), 1).also {
						it.ingredients = listOf(
							ItemStack(Material.DIRT, 2),
						)
					}
				)
			}
		}
	}
}