package se.ade.mc.cubematic.extensions

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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

fun ItemStack.spend(amount: Int = 1): ItemStack {
	return if (this.amount <= amount) {
		ItemStack.empty()
	} else {
		this.clone().also { it.amount -= amount }
	}
}

infix operator fun ItemStack.minus(amount: Int): ItemStack {
	return this.spend(amount)
}