package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.StonecuttingRecipe
import se.ade.mc.skyblock.CubematicSkyPlugin

/**
 * Makes glass (more practically) renewable by allowing stonecutter to cut glass blocks and glass bottles into sand.
 */
fun cutGlassRule(plugin: CubematicSkyPlugin) {
	plugin.server.addRecipe(
		StonecuttingRecipe(
			/*key = */ NamespacedKey(plugin, "stonecutter_glass_bottle"),
			/*result = */ ItemStack(Material.SAND),
			/*source = */ Material.GLASS_BOTTLE,
		)
	)
	plugin.server.addRecipe(
		StonecuttingRecipe(
			/*key = */ NamespacedKey(plugin, "stonecutter_glass_block"),
			/*result = */ ItemStack(Material.SAND),
			/* input = */
			RecipeChoice.MaterialChoice(
				listOf(
					Material.GLASS,
					Material.TINTED_GLASS,

					Material.BLACK_STAINED_GLASS,
					Material.BLUE_STAINED_GLASS,
					Material.BROWN_STAINED_GLASS,
					Material.CYAN_STAINED_GLASS,
					Material.GRAY_STAINED_GLASS,
					Material.GREEN_STAINED_GLASS,
					Material.LIGHT_BLUE_STAINED_GLASS,
					Material.LIGHT_GRAY_STAINED_GLASS,
					Material.LIME_STAINED_GLASS,
					Material.MAGENTA_STAINED_GLASS,
					Material.ORANGE_STAINED_GLASS,
					Material.PINK_STAINED_GLASS,
					Material.PURPLE_STAINED_GLASS,
					Material.RED_STAINED_GLASS,
					Material.WHITE_STAINED_GLASS,
					Material.YELLOW_STAINED_GLASS,
				)
			),
		)
	)
}