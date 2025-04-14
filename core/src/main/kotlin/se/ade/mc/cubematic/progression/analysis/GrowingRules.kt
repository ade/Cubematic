package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.entity.EntityType

fun DependencyGraphBuilderScope.growingRules() {
	farmingCrops()
	logGrowing()

	item(Material.APPLE) {
		from {
			grow(plant = Material.OAK_SAPLING, on = saplingSoils())
		}
	}
	item(Material.BEEHIVE) {
		from {
			grow(plant = Material.OAK_SAPLING, on = saplingSoils())
		}
	}
	entity(EntityType.BEE) {
		from {
			grow(plant = Material.OAK_SAPLING, on = saplingSoils())
		}
	}
	item(Material.HONEYCOMB) {
		from {
			entity(EntityType.BEE)
			having(Material.BEEHIVE)
		}
	}
	item(Material.HONEY_BOTTLE) {
		from {
			entity(EntityType.BEE)
			having(Material.BEEHIVE, Material.GLASS_BOTTLE)
		}
	}
}

private fun DependencyGraphBuilderScope.farmingCrops() {
	item(Material.FARMLAND) {
		from {
			having(Material.DIRT, Material.WATER)
		}
	}
	item(Material.WHEAT) {
		from {
			grow(Material.WHEAT_SEEDS, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.BEETROOT) {
		from {
			grow(Material.BEETROOT_SEEDS, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.CARROT) {
		from {
			grow(Material.CARROT, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.POTATO) {
		from {
			grow(Material.POTATO, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.MELON) {
		from {
			grow(Material.MELON_SEEDS, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.PUMPKIN) {
		from {
			grow(Material.PUMPKIN_SEEDS, on = anyOf(Material.FARMLAND))
		}
	}
	item(Material.NETHER_WART) {
		from {
			grow(Material.NETHER_WART, on = anyOf(Material.SOUL_SAND))
		}
	}
	item(Material.CACTUS) {
		from {
			grow(Material.CACTUS, on = anyOf(Material.SAND, Material.RED_SAND))
		}
	}
	item(Material.SUGAR_CANE) {
		from {
			grow(Material.SUGAR_CANE, on = anyOf(Material.GRASS_BLOCK, Material.DIRT, Material.SAND))
		}
	}
	item(Material.BAMBOO) {
		from { grow(plant = Material.BAMBOO, on = saplingSoils() + anyOf(
			Material.SAND, Material.RED_SAND, Material.GRAVEL, Material.SUSPICIOUS_SAND, Material.SUSPICIOUS_GRAVEL)) }
	}
	item(Material.KELP) {
		from {
			grow(Material.KELP, on = anyOf(Material.DIRT))
		}
	}
}

fun DependencyGraphBuilderScope.logGrowing() {
	item(Material.OAK_LOG) {
		from { grow(plant = Material.OAK_SAPLING, on = saplingSoils()) }
	}
	item(Material.BIRCH_LOG) {
		from { grow(plant = Material.BIRCH_SAPLING, on = saplingSoils()) }
	}
	item(Material.SPRUCE_LOG) {
		from { grow(plant = Material.SPRUCE_SAPLING, on = saplingSoils()) }
	}
	item(Material.JUNGLE_LOG) {
		from { grow(plant = Material.JUNGLE_SAPLING, on = saplingSoils()) }
	}
	item(Material.ACACIA_LOG) {
		from { grow(plant = Material.ACACIA_SAPLING, on = saplingSoils()) }
	}
	item(Material.DARK_OAK_LOG) {
		from { grow(plant = Material.DARK_OAK_SAPLING, on = saplingSoils()) }
	}
	item(Material.PALE_OAK_LOG) {
		from { grow(plant = Material.PALE_OAK_SAPLING, on = saplingSoils()) }
	}
	item(Material.MANGROVE_LOG) {
		from { grow(plant = Material.MANGROVE_PROPAGULE, on = saplingSoils()) }
	}
	item(Material.CHERRY_LOG) {
		from { grow(plant = Material.CHERRY_SAPLING, on = saplingSoils()) }
	}
	item(Material.WARPED_STEM) {
		from { grow(plant = Material.WARPED_FUNGUS, on = saplingSoils() + anyOf(Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.SOUL_SOIL)) }
	}
	item(Material.CRIMSON_STEM) {
		from { grow(plant = Material.CRIMSON_FUNGUS, on = saplingSoils() + anyOf(Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM, Material.SOUL_SOIL)) }
	}
}