package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material

fun DependencyGraphBuilderScope.growingRules() {
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