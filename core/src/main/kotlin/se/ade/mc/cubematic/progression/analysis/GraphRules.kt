package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.potion.PotionType
import se.ade.mc.cubematic.progression.analysis.key.ItemTag

fun DependencyGraphBuilderScope.standardRules() {
	growingRules()
	spawningEntities()
	wanderingTraderRules()
	villagerTradingRules()

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
	mechanic(MechanicType.NETHER) {
		from {
			having(Material.OBSIDIAN, Material.FLINT_AND_STEEL)
		}
	}
	item(Material.LAVA) {
		from("Harvest Lava with Dripstone+Cauldron") {
			having(Material.LAVA_BUCKET, Material.CAULDRON, Material.POINTED_DRIPSTONE)
		}
	}
	item(Material.POINTED_DRIPSTONE) {
		from("Farming dripstone") {
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
	item(Material.OBSIDIAN) {
		from("Turn lava into obsidian") {
			having(Material.LAVA, Material.WATER)
		}
	}
}