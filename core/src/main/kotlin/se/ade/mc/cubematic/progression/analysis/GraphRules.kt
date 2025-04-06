package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.potion.PotionType
import se.ade.mc.cubematic.progression.analysis.key.ItemTag

fun DependencyGraphBuilderScope.standardRules() {
	logGrowing()
	growingRules()
	wanderingTraderRules()

	mechanic(MechanicType.VILLAGER_TRADING) {
		from {
			having(Material.GOLDEN_APPLE,
				//TODO Splash potion of weakness as key
				Material.BREWING_STAND,
				Material.SPIDER_EYE,
				Material.SUGAR,
				Material.GUNPOWDER,
				Material.BROWN_MUSHROOM,
				Material.BLAZE_ROD,
				Material.GLASS_BOTTLE)
		}
	}
	item(Material.LAVA) {
		from {
			having(Material.LAVA, Material.CAULDRON, Material.POINTED_DRIPSTONE)
		}
	}
	item(Material.POINTED_DRIPSTONE) {
		from {
			having(Material.DRIPSTONE_BLOCK, Material.WATER)
		}
	}
	item(Material.WATER) {
		from {
			having(ProcessRequirement.Any(anyOf(Material.WATER_BUCKET, Material.ICE), 1))
		}
	}
	item(Material.COBBLESTONE) {
		from {
			having(Material.LAVA_BUCKET, Material.WATER)
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