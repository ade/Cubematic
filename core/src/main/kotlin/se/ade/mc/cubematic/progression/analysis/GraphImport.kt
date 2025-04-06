package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.inventory.BrewerInventory
import org.bukkit.inventory.FurnaceRecipe
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.plugin.java.JavaPlugin

fun DependencyGraphBuilderScope.importAll(plugin: JavaPlugin) {
	val recipes = plugin.server.recipeIterator()
	
	val all = recipes.asSequence().toList()

	all.forEach { recipe ->
		val material = recipe.result.type
		when(recipe) {
			is ShapedRecipe -> {
				val ingredientCount = mutableMapOf<Material, Int>().apply {
					val shape = recipe.shape.joinToString("")
					val ingredients = recipe.ingredientMap

					shape.forEach { char ->
						if (char != ' ') {
							ingredients[char]?.let { ingredient ->
								val key = ingredient.type
								this[key] = getOrDefault(key, 0) + 1
							}
						}
					}
				}


				val handCraft = recipe.shape.size <= 2 &&
						recipe.shape[0].length <= 2 &&
						(recipe.shape.size == 1 || recipe.shape[1].length <= 2)

				val ingredients = ingredientCount.map { (material, count) ->
					count of material
				}.toTypedArray()

				item(material) {
					from {
						if(handCraft) {
							craftByHand(*ingredients)
						} else {
							crafting(*ingredients)
						}
					}
				}
			}
			is ShapelessRecipe -> {
				val ingredientCount = mutableMapOf<Material, Int>().apply {
					recipe.ingredientList.forEach { ingredient ->
						val key = ingredient.type
						this[key] = getOrDefault(key, 0) + 1
					}
				}

				val ingredients = ingredientCount.map { (material, count) ->
					count of material
				}.toTypedArray()

				val sum = ingredientCount.values.sum()

				item(material) {
					from {
						if(sum <= 4)
							craftByHand(*ingredients)
						else
							crafting(*ingredients)
					}
				}
			}
			is FurnaceRecipe -> {
				val output = recipe.result.type

				when(val input = recipe.inputChoice) {
					is RecipeChoice.MaterialChoice -> {
						item(output) {
							from {
								smelting(anyOf(*input.choices.toTypedArray()), with = furnace())
							}
						}
					}
					is RecipeChoice.ExactChoice -> {
						// UNSUPPORTED
					}
				}
			}
			is MerchantRecipe -> {
				plugin.logger.info {
					"Merchant recipe: ${recipe.result.type} -> ${recipe.ingredients.joinToString(", ") { it.type.toString() }}"
				}
			}
			else -> {
				// UNSUPPORTED
			}
		}
	}

}
