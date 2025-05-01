package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.CampfireRecipe
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.definitions.overworldLogTypes

fun charcoalOnCampfireRecipe(plugin: JavaPlugin) {
	plugin.server.addRecipe(CampfireRecipe(
		NamespacedKey(plugin, "campfired_log"),
		ItemStack(Material.CHARCOAL),
		RecipeChoice.MaterialChoice(*overworldLogTypes.toTypedArray()),
		0f,
		20 * 60 * 2, // 2 minutes
	))
}