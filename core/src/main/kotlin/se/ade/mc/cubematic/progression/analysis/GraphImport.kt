package se.ade.mc.cubematic.progression.analysis

import org.bukkit.Material
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin

fun DependencyGraphBuilderScope.importAll(plugin: JavaPlugin) {
	val recipes = plugin.server.recipeIterator()
	
	val all = recipes.asSequence().toList()

	all.forEach { recipe ->
		val material = recipe.result.type
		when(recipe) {
			is ShapedRecipe -> {
				importShapedRecipe(recipe, plugin)
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

private fun DependencyGraphBuilderScope.importShapedRecipe(recipe: ShapedRecipe, plugin: JavaPlugin) {
	val shape = recipe.shape.joinToString("")

	// Count occurrences of each material in the shape
	val ingredientCounts = mutableMapOf<Char, Int>()
	shape.forEach { char ->
		if (char != ' ') {
			ingredientCounts[char] = ingredientCounts.getOrDefault(char, 0) + 1
		}
	}

	val requirements = ingredientCounts.mapNotNull { (char, count) ->
		when(val recipeChoice = recipe.choiceMap[char]) {
			is RecipeChoice.MaterialChoice -> {
				if(recipeChoice.choices.size == 1) {
					// Handle single material choice
					count of recipeChoice.choices.first()
				} else {
					// Handle multiple material choices
					ProcessRequirement.Any(
						filter = anyOf(*recipeChoice.choices.toTypedArray()),
						quantity = count
					)
				}
			}
			else -> null
		}
	}

	// Merge similar requirements
	val merged = mutableListOf<ProcessRequirement>()
	requirements.forEach { requirement ->
		val existing = merged.find {
			(it is ProcessRequirement.Any &&
					requirement is ProcessRequirement.Any &&
					it.filter == requirement.filter
					) ||
					(it is ProcessRequirement.Type &&
							requirement is ProcessRequirement.Type
							&& requirement.key == it.key)
		}

		val replacement = when {
			existing is ProcessRequirement.Any && requirement is ProcessRequirement.Any -> {
				// Merge quantities
				ProcessRequirement.Any(existing.filter, existing.quantity + requirement.quantity)
			}
			existing is ProcessRequirement.Type && requirement is ProcessRequirement.Type -> {
				// Merge quantities
				ProcessRequirement.Type(existing.key, existing.quantity + requirement.quantity)
			}
			else -> null
		}
		if(replacement != null) {
			merged.remove(existing)
			merged.add(replacement)
		} else {
			merged.add(requirement)
		}
	}

	val handCraft = recipe.shape.size <= 2 &&
			recipe.shape[0].length <= 2 &&
			(recipe.shape.size == 1 || recipe.shape[1].length <= 2)

	if(recipe.result.type == Material.WHITE_BED) {
		plugin.logger.info {
			"Recipe: ${recipe.result.type} -> ${recipe.shape.joinToString(", ")}"
		}
		plugin.logger.info {
			"Requirements: ${merged.joinToString(", ")}"
		}
	}

	item(recipe.result.type) {
		from {
			if(handCraft) {
				craftByHand(*merged.toTypedArray())
			} else {
				crafting(*merged.toTypedArray())
			}
		}
	}
}