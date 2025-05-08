package se.ade.mc.skyblock.trader

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.EntityType
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.persistence.PersistentDataType
import se.ade.mc.skyblock.CubematicSkyPlugin

private val shardRecipes = listOf(
	ItemStack(Material.HONEYCOMB_BLOCK),
	ItemStack(Material.COPPER_BLOCK),
	ItemStack(Material.CREEPER_HEAD),
	ItemStack(Material.SKELETON_SKULL),
	ItemStack(Material.ZOMBIE_HEAD),
	ItemStack(Material.CAKE),
	ItemStack(Material.GLOW_ITEM_FRAME),
	ItemStack(Material.JACK_O_LANTERN),
	ItemStack(Material.LECTERN),
	ItemStack(Material.PUMPKIN_PIE),
	ItemStack(Material.SEA_LANTERN),
	ItemStack(Material.BAMBOO_BLOCK),
	ItemStack(Material.TADPOLE_BUCKET),
	ItemStack(Material.AXOLOTL_BUCKET),
	ItemStack(Material.DRIED_KELP_BLOCK)
)

fun traderLavaBucketRule(event: CreatureSpawnEvent, plugin: CubematicSkyPlugin) {
	val moddedKey = NamespacedKey(plugin, "trader-lava-bucket-recipe")

	if(event.entity.type == EntityType.WANDERING_TRADER) {
		val trader = (event.entity as WanderingTrader)

		if(trader.persistentDataContainer.get(moddedKey, PersistentDataType.BOOLEAN) == true) {
			// Already modified
			return
		}

		trader.persistentDataContainer.set(moddedKey, PersistentDataType.BOOLEAN, true)

		val shardTrades = getShardTrades(plugin)

		val bucketTrade = MerchantRecipe(ItemStack(Material.LAVA_BUCKET), 1).also {
			it.ingredients = listOf(
				ItemStack(Material.ECHO_SHARD, 10),
			)
			it.maxUses = 1
		}

		trader.recipes = trader.recipes + shardTrades + bucketTrade
	}
}

/**
 * Get the trades for the trader that were previously set in the config,
 * or generate new ones if they are not set / not found
 */
private fun getShardTrades(plugin: CubematicSkyPlugin): List<MerchantRecipe> {
	val lastItemKeys = plugin.config.trader.shardIngredientKeys.mapNotNull {
		val key = NamespacedKey.fromString(it)

		Material.entries.firstOrNull { m ->
			m.key == key
		}?.let { found ->
			ItemStack(found)
		}
	}

	val amountShardTrades = 2
	val amountMissing = (amountShardTrades - lastItemKeys.size).coerceIn(0, amountShardTrades)
	val ingredients: List<ItemStack> = lastItemKeys + if(amountMissing > 0) {
		// Fill with random items
		(0 until amountMissing).map {
			shardRecipes.random()
		}
	} else {
		emptyList()
	}

	if(lastItemKeys.toSet() != ingredients.toSet()) {
		// Update the config with the new items
		plugin.config = plugin.config.copy(
			trader = plugin.config.trader.copy(
				shardIngredientKeys = ingredients.map { it.type.key.toString() }
			)
		)
	}

	val shardTrades = ingredients.map { stack ->
		MerchantRecipe(ItemStack(Material.ECHO_SHARD), 1).also {
			it.ingredients = listOf(stack)
			it.maxUses = 1
		}
	}

	return shardTrades
}