package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.entity.EntityType

fun DependencyGraphBuilderScope.spawningEntities() {
	spawnable()
	loot()
}

private fun DependencyGraphBuilderScope.spawnable() {
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

private fun DependencyGraphBuilderScope.loot() {
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