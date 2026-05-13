package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.entity.EntityType

fun DependencyGraphBuilderScope.spawningEntities() {
	animals()
	animalLoot()

	monsters()
	mobLoot()

	netherMobs()
	netherMobLoot()
}

private fun DependencyGraphBuilderScope.monsters() {
	entity(EntityType.ZOMBIE) {
		from {
			overworld()
		}
	}
	entity(EntityType.SKELETON) {
		from {
			overworld()
		}
	}
	entity(EntityType.CREEPER) {
		from {
			overworld()
		}
	}
	entity(EntityType.SPIDER) {
		from {
			overworld()
		}
	}
	entity(EntityType.ENDERMAN) {
		from {
			overworld()
		}
	}
	entity(EntityType.SLIME) {
		from {
			overworld()
		}
	}
	entity(EntityType.PHANTOM) {
		from {
			overworld()
		}
	}
	entity(EntityType.WITCH) {
		from {
			overworld()
		}
	}
	entity(EntityType.DROWNED) {
		from {
			overworld()
		}
	}
}

private fun DependencyGraphBuilderScope.mobLoot() {
	item(Material.ROTTEN_FLESH) {
		fromEntity(EntityType.ZOMBIE)
	}
	item(Material.BONE) {
		fromEntity(EntityType.SKELETON)
	}
	item(Material.ARROW) {
		fromEntity(EntityType.SKELETON)
	}
	item(Material.STRING) {
		fromEntity(EntityType.SPIDER)
	}
	item(Material.ENDER_PEARL) {
		fromEntity(EntityType.ENDERMAN)
	}
	item(Material.SLIME_BALL) {
		fromEntity(EntityType.SLIME)
	}
	item(Material.COPPER_INGOT) {
		fromEntity(EntityType.DROWNED)
	}
}

private fun DependencyGraphBuilderScope.netherMobs() {
	entity(EntityType.GHAST) {
		from {
			nether()
		}
	}
	entity(EntityType.MAGMA_CUBE) {
		from {
			nether()
		}
	}
	entity(EntityType.PIGLIN) {
		from {
			nether()
		}
	}
	entity(EntityType.HOGLIN) {
		from {
			nether()
		}
	}
	entity(EntityType.ZOMBIFIED_PIGLIN) {
		from {
			nether()
		}
	}
	entity(EntityType.BLAZE) {
		from {
			nether()

			// Mod: Allow spawning on nether bricks
			having(Material.NETHER_BRICKS)
		}
	}
	entity(EntityType.WITHER_SKELETON) {
		from {
			nether()

			// Mod: Allow spawning on nether bricks
			having(Material.NETHER_BRICKS)
		}
	}
}

private fun DependencyGraphBuilderScope.netherMobLoot() {
	item(Material.GOLD_INGOT) {
		fromEntity(EntityType.ZOMBIFIED_PIGLIN)
	}
	item(Material.GOLD_NUGGET) {
		fromEntity(EntityType.ZOMBIFIED_PIGLIN)
	}
	item(Material.WITHER_ROSE) {
		fromEntity(EntityType.WITHER_SKELETON)
	}
	item(Material.WITHER_SKELETON_SKULL) {
		fromEntity(EntityType.WITHER_SKELETON)
	}
	item(Material.BLAZE_ROD) {
		fromEntity(EntityType.BLAZE)
	}
	item(Material.MAGMA_CREAM) {
		fromEntity(EntityType.MAGMA_CUBE)
	}
	item(Material.GHAST_TEAR) {
		fromEntity(EntityType.GHAST)
	}

	// Mod: Piglin drops netherrack
	item(Material.NETHERRACK) {
		fromEntity(EntityType.ZOMBIFIED_PIGLIN)
	}
}

private fun DependencyGraphBuilderScope.animals() {
	entity(EntityType.COW) {
		from {
			overworld()
		}
	}
	entity(EntityType.SHEEP) {
		from {
			overworld()
		}
	}
	entity(EntityType.PIG) {
		from {
			overworld()
		}
	}
	entity(EntityType.CHICKEN) {
		from {
			overworld()
		}
	}
	entity(EntityType.RABBIT) {
		from {
			overworld()
		}
	}
	entity(EntityType.WOLF) {
		from {
			overworld()
		}
	}
	entity(EntityType.SQUID) {
		from {
			overworld()
		}
	}
	entity(EntityType.SALMON) {
		from {
			overworld()
		}
	}
	entity(EntityType.TROPICAL_FISH) {
		from {
			overworld()
		}
	}
	entity(EntityType.DOLPHIN) {
		from {
			overworld()
		}
	}
	entity(EntityType.PARROT) {
		from {
			overworld()
		}
	}
	entity(EntityType.BEE) {
		from {
			overworld()
		}
	}
	entity(EntityType.HORSE) {
		from {
			overworld()
		}
	}
	entity(EntityType.DONKEY) {
		from {
			overworld()
		}
	}
	entity(EntityType.MULE) {
		from {
			overworld()
		}
	}
}

private fun DependencyGraphBuilderScope.animalLoot() {
	item(Material.WHITE_WOOL) {
		fromEntity(EntityType.SHEEP)
	}
	item(Material.LEATHER) {
		fromEntity(EntityType.COW)
	}
	item(Material.MUTTON) {
		fromEntity(EntityType.SHEEP)
	}
	item(Material.RABBIT_FOOT) {
		fromEntity(EntityType.RABBIT)
	}
	item(Material.RABBIT_HIDE) {
		fromEntity(EntityType.RABBIT)
	}
	item(Material.RABBIT) {
		fromEntity(EntityType.RABBIT)
	}
	item(Material.PORKCHOP) {
		fromEntity(EntityType.PIG)
	}
	item(Material.BEEF) {
		fromEntity(EntityType.COW)
	}
	item(Material.CHICKEN) {
		fromEntity(EntityType.CHICKEN)
	}
	item(Material.FEATHER) {
		fromEntity(EntityType.CHICKEN)
	}
	item(Material.EGG) {
		fromEntity(EntityType.CHICKEN)
	}
}