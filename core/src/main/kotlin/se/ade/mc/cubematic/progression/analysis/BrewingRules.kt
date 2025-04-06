package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.potion.PotionType
import se.ade.mc.cubematic.progression.analysis.key.ItemTag
import se.ade.mc.cubematic.progression.analysis.key.NodeKey

fun DependencyGraphBuilderScope.brewingRecipes() {
	brewingTier1()
	brewingTier2()
	brewingTier3()
	brewingStrong()

	// Splash potions
	PotionType.entries.forEach { type ->
		item(Material.SPLASH_POTION, setOf(ItemTag.Potion(type))) {
			from {
				brewing(
					1 of Material.GUNPOWDER,
					1 of potion(type),
				)
			}
		}
	}

	// Lingering potions
	PotionType.entries.forEach { type ->
		item(Material.LINGERING_POTION, setOf(ItemTag.Potion(type))) {
			from {
				brewing(
					1 of Material.DRAGON_BREATH,
					1 of NodeKey.Item(Material.SPLASH_POTION, setOf(ItemTag.Potion(type))),
				)
			}
		}
	}

	// TODO Extended duration potions are not supported
}

private fun DependencyGraphBuilderScope.brewingTier1() {
	item(potion(PotionType.WEAKNESS)) {
		from {
			brewing(Material.FERMENTED_SPIDER_EYE)
		}
	}
	item(potion(PotionType.AWKWARD)) {
		from {
			brewing(1 of Material.NETHER_WART)
		}
	}
}

private fun DependencyGraphBuilderScope.brewingTier2() {
	item(potion(PotionType.SWIFTNESS)) {
		from {
			brewing(
				1 of Material.SUGAR,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.FIRE_RESISTANCE)) {
		from {
			brewing(
				1 of Material.MAGMA_CREAM,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.NIGHT_VISION)) {
		from {
			brewing(
				1 of Material.GOLDEN_CARROT,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.LEAPING)) {
		from {
			brewing(
				1 of Material.RABBIT_FOOT,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}

	item(potion(PotionType.POISON)) {
		from {
			brewing(
				1 of Material.SPIDER_EYE,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.HEALING)) {
		from {
			brewing(
				1 of Material.GLISTERING_MELON_SLICE,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}

	item(potion(PotionType.STRENGTH)) {
		from {
			brewing(
				1 of Material.BLAZE_POWDER,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.REGENERATION)) {
		from {
			brewing(
				1 of Material.GHAST_TEAR,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.WATER_BREATHING)) {
		from {
			brewing(
				1 of Material.PUFFERFISH,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.TURTLE_MASTER)) {
		from {
			brewing(
				1 of Material.TURTLE_SCUTE,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.SLOW_FALLING)) {
		from {
			brewing(
				1 of Material.PHANTOM_MEMBRANE,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.WIND_CHARGED)) {
		from {
			brewing(
				1 of Material.BREEZE_ROD,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.WEAVING)) {
		from {
			brewing(
				1 of Material.COBWEB,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.OOZING)) {
		from {
			brewing(
				1 of Material.SLIME_BLOCK,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
	item(potion(PotionType.INFESTED)) {
		from {
			brewing(
				1 of Material.STONE,
				1 of potion(PotionType.AWKWARD),
			)
		}
	}
}

private fun DependencyGraphBuilderScope.brewingTier3() {
	item(potion(PotionType.INVISIBILITY)) {
		from {
			brewing(
				1 of Material.FERMENTED_SPIDER_EYE,
				1 of potion(PotionType.NIGHT_VISION),
			)
		}
	}
	item(potion(PotionType.SLOWNESS)) {
		from {
			brewing(
				1 of Material.FERMENTED_SPIDER_EYE,
				1 of potion(PotionType.SWIFTNESS),
			)
		}
	}
	item(potion(PotionType.SLOWNESS)) {
		from {
			brewing(
				1 of Material.FERMENTED_SPIDER_EYE,
				1 of potion(PotionType.LEAPING),
			)
		}
	}
	item(potion(PotionType.HARMING)) {
		from {
			brewing(
				1 of Material.FERMENTED_SPIDER_EYE,
				1 of potion(PotionType.HEALING),
			)
		}
	}
	item(potion(PotionType.HARMING)) {
		from {
			brewing(
				1 of Material.FERMENTED_SPIDER_EYE,
				1 of potion(PotionType.POISON),
			)
		}
	}
}

private fun DependencyGraphBuilderScope.brewingStrong() {
	item(potion(PotionType.STRONG_HARMING)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.HARMING),
			)
		}
	}
	item(potion(PotionType.STRONG_HEALING)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.HEALING),
			)
		}
	}
	item(potion(PotionType.STRONG_LEAPING)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.LEAPING),
			)
		}
	}
	item(potion(PotionType.STRONG_POISON)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.POISON),
			)
		}
	}
	item(potion(PotionType.STRONG_REGENERATION)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.REGENERATION),
			)
		}
	}
	item(potion(PotionType.STRONG_SLOWNESS)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.SLOWNESS),
			)
		}
	}
	item(potion(PotionType.STRONG_STRENGTH)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.STRENGTH),
			)
		}
	}
	item(potion(PotionType.STRONG_SWIFTNESS)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.SWIFTNESS),
			)
		}
	}
	item(potion(PotionType.STRONG_TURTLE_MASTER)) {
		from {
			brewing(
				1 of Material.GLOWSTONE_DUST,
				1 of potion(PotionType.TURTLE_MASTER),
			)
		}
	}
}

internal fun potion(type: PotionType): NodeKey.Item {
	return NodeKey.Item(Material.POTION, setOf(ItemTag.Potion(type)))
}