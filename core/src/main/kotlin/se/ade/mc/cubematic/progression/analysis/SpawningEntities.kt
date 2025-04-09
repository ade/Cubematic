package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.entity.EntityType

fun DependencyGraphBuilderScope.spawningEntities() {
	item(Material.WHITE_WOOL) {
		fromOverworldEntity(EntityType.SHEEP)
	}
	item(Material.LEATHER) {
		fromOverworldEntity(EntityType.COW)
	}
	item(Material.MUTTON) {
		fromOverworldEntity(EntityType.SHEEP)
	}
	item(Material.RABBIT_FOOT) {
		fromOverworldEntity(EntityType.RABBIT)
	}
	item(Material.RABBIT_HIDE) {
		fromOverworldEntity(EntityType.RABBIT)
	}
	item(Material.RABBIT) {
		fromOverworldEntity(EntityType.RABBIT)
	}
	item(Material.PORKCHOP) {
		fromOverworldEntity(EntityType.PIG)
	}
	item(Material.BEEF) {
		fromOverworldEntity(EntityType.COW)
	}
	item(Material.CHICKEN) {
		fromOverworldEntity(EntityType.CHICKEN)
	}
	item(Material.FEATHER) {
		fromOverworldEntity(EntityType.CHICKEN)
	}
	item(Material.EGG) {
		fromOverworldEntity(EntityType.CHICKEN)
	}
}