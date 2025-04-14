package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.potion.PotionType

fun DependencyGraphBuilderScope.wanderingTraderRules() {
	val description = "Wandering Trader"

	item(Material.EMERALD) {
		from(description) {
			wanderingTrader(1 of Material.HAY_BLOCK)
		}
		from(description) {
			wanderingTrader(1 of Material.GLASS_BOTTLE)
		}
		from(description) {
			wanderingTrader(1 of Material.BAKED_POTATO)
		}
		from(description) {
			wanderingTrader(1 of Material.MILK_BUCKET)
		}
		from(description) {
			wanderingTrader(1 of Material.WATER_BUCKET)
		}
		from(description) {
			wanderingTrader(1 of Material.FERMENTED_SPIDER_EYE)
		}
	}

	item(potion(PotionType.INVISIBILITY)) {
		from(description) {
			wanderingTrader(1 of Material.EMERALD)
		}
	}
	overworldLogTypes.forEach {
		item(it) {
			from(description) {
				wanderingTrader(1 of Material.EMERALD)
			}
		}
	}

	val purchasable = setOf<Material>(
		Material.EMERALD,

		Material.PACKED_ICE,
		Material.BLUE_ICE,
		Material.GUNPOWDER,
		Material.PODZOL,
		Material.IRON_PICKAXE,

		Material.FERN,
		Material.SUGAR_CANE,
		Material.PUMPKIN,
		Material.DANDELION,
		Material.POPPY,
		Material.ALLIUM,
		Material.AZURE_BLUET,
		Material.RED_TULIP,
		Material.ORANGE_TULIP,
		Material.WHITE_TULIP,
		Material.PINK_TULIP,
		Material.OXEYE_DAISY,
		Material.CORNFLOWER,
		Material.BLUE_ORCHID,
		Material.LILY_OF_THE_VALLEY,
		Material.OPEN_EYEBLOSSOM,
		Material.WHEAT_SEEDS,
		Material.BEETROOT_SEEDS,
		Material.PUMPKIN_SEEDS,
		Material.MELON_SEEDS,
		Material.VINE,
		Material.PALE_HANGING_MOSS,
		Material.BROWN_MUSHROOM,
		Material.RED_MUSHROOM,
		Material.LILY_PAD,
		Material.SMALL_DRIPLEAF,
		Material.SAND,
		Material.RED_SAND,
		Material.POINTED_DRIPSTONE,
		Material.ROOTED_DIRT,
		Material.MOSS_BLOCK,
		Material.PALE_MOSS_BLOCK,
		Material.SEA_PICKLE,
		Material.GLOWSTONE,
		Material.TROPICAL_FISH_BUCKET,
		Material.PUFFERFISH_BUCKET,
		Material.KELP,
		Material.CACTUS,
		Material.BRAIN_CORAL_BLOCK,
		Material.BUBBLE_CORAL_BLOCK,
		Material.FIRE_CORAL_BLOCK,
		Material.HORN_CORAL_BLOCK,
		Material.TUBE_CORAL_BLOCK,
		Material.SLIME_BALL,
		Material.NAUTILUS_SHELL,
		Material.FIREFLY_BUSH,
		Material.TALL_DRY_GRASS,
		Material.WILDFLOWERS
	)

	purchasable.forEach {
		item(it) {
			from(description) {
				wanderingTrader(1 of Material.EMERALD)
			}
		}
	}
}