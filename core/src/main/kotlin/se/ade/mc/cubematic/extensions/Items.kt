package se.ade.mc.cubematic.extensions

import org.bukkit.Material

enum class PickTier(val level: Int, vararg materials: Material) {
	WOODEN(0, Material.WOODEN_PICKAXE, Material.GOLDEN_PICKAXE),
	STONE(1, Material.STONE_PICKAXE),
	IRON(2, Material.IRON_PICKAXE),
	DIAMOND(3, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE);

	val materials = materials.toSet()
}

infix fun Material.isPickOfAtleast(level: PickTier): Boolean {
	return PickTier.entries.filter { it.level >= level.level }
		.any { it.materials.contains(this) }
}