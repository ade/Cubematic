package se.ade.mc.wiki.md.main.nodes

import org.sweble.wikitext.parser.nodes.WtTemplate
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtInternalLink
import org.sweble.wikitext.parser.nodes.WtName
import se.ade.mc.wiki.md.MarkdownDocumentBuilder

/**
 * WikiText template to text converter for crafting recipes.
 * Supports both shaped and shapeless recipes, with multiple alternatives per slot.
 * Outputs multiple recipes if there are alternatives.
 */
object Crafting {
	/**
	 * Intermediate representation of a crafting recipe
	 */
	data class CraftingRecipe(
		val name: String?,
		val ingredients: String?,
		val description: String?,
		val grid: CraftingGrid,
		val output: SlotContent?,
		val type: String?,
		val isShapeless: Boolean,
		val showName: Boolean,
		val showDescription: Boolean
	)

	data class CraftingGrid(
		val a1: SlotContent?,
		val b1: SlotContent?,
		val c1: SlotContent?,
		val a2: SlotContent?,
		val b2: SlotContent?,
		val c2: SlotContent?,
		val a3: SlotContent?,
		val b3: SlotContent?,
		val c3: SlotContent?
	) {
		fun isEmpty(): Boolean = listOf(a1, b1, c1, a2, b2, c2, a3, b3, c3).all { it == null }

		fun getAllSlots(): List<SlotContent?> = listOf(a1, a2, a3, b1, b2, b3, c1, c2, c3)

		fun nonEmptyCount(): Int = getAllSlots().count { it != null }
	}

	data class SlotContent(
		val items: List<String>
	) {
		fun format(): String = items.joinToString(" / ")
	}

	/**
	 * Handle the crafting template.
	 */
	fun visit(node: WtTemplate, builder: MarkdownDocumentBuilder) {
		val recipe = parseTemplate(node)
		val expandedRecipes = expandRecipe(recipe)

		expandedRecipes.forEachIndexed { index, expandedRecipe ->
			if (index > 0) {
				builder.horizontalRule()
			}
			convertToBuilder(expandedRecipe, builder)
		}
	}

	/**
	 * Expand a recipe with multiple alternatives into separate recipes
	 */
	private fun expandRecipe(recipe: CraftingRecipe): List<CraftingRecipe> {
		// Find the maximum number of alternatives in any slot
		val maxAlternatives = maxOf(
			recipe.grid.a1?.items?.size ?: 1,
			recipe.grid.b1?.items?.size ?: 1,
			recipe.grid.c1?.items?.size ?: 1,
			recipe.grid.a2?.items?.size ?: 1,
			recipe.grid.b2?.items?.size ?: 1,
			recipe.grid.c2?.items?.size ?: 1,
			recipe.grid.a3?.items?.size ?: 1,
			recipe.grid.b3?.items?.size ?: 1,
			recipe.grid.c3?.items?.size ?: 1,
			recipe.output?.items?.size ?: 1
		)

		// If there's only one alternative everywhere, return the recipe as-is
		if (maxAlternatives == 1) {
			return listOf(recipe)
		}

		// Create separate recipes for each alternative
		val expandedRecipes = mutableListOf<CraftingRecipe>()

		for (i in 0 until maxAlternatives) {
			val expandedGrid = CraftingGrid(
				a1 = recipe.grid.a1?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				b1 = recipe.grid.b1?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				c1 = recipe.grid.c1?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				a2 = recipe.grid.a2?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				b2 = recipe.grid.b2?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				c2 = recipe.grid.c2?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				a3 = recipe.grid.a3?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				b3 = recipe.grid.b3?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) },
				c3 = recipe.grid.c3?.let { slot -> SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() })) }
			)

			val expandedOutput = recipe.output?.let { slot ->
				SlotContent(listOf(slot.items.getOrElse(i) { slot.items.last() }))
			}

			expandedRecipes.add(
				recipe.copy(
					grid = expandedGrid,
					output = expandedOutput,
					// Only show name on first recipe
					showName = recipe.showName && i == 0,
					// Only show ingredients on first recipe
					ingredients = if (i == 0) recipe.ingredients else null
				)
			)
		}

		return expandedRecipes
	}

	private fun extractParameters(node: WtTemplate): Map<String, String> {
		val params = mutableMapOf<String, String>()
		var positionalIndex = 1

		for (arg in node.args) {
			if (arg is WtTemplateArgument) {
				// Extract value from all node types, not just WtText
				val parts = mutableListOf<String>()
				for (valueNode in arg.value) {
					when (valueNode) {
						is WtText -> {
							parts.add(valueNode.content)
						}
						is WtInternalLink -> {
							// Extract link text: [[Page]] or [[Page|Display]]
							val target = valueNode.target.asString ?: ""
							parts.add(target)
						}
						// Add other node types as needed
						else -> {
							// Try to extract text from other nodes
							parts.add(valueNode.toString())
						}
					}
				}

				val value = parts.joinToString("").trim()

				if (value.isEmpty()) continue

				// Check if this is a named or positional parameter
				val argName = if (arg.name is WtName.WtNoName) null else arg.name?.asString?.trim()

				if (argName != null && argName.isNotEmpty()) {
					// Named parameter
					params[argName] = value
				} else {
					// Positional parameter - map to grid positions or ingredients
					params["pos$positionalIndex"] = value
					positionalIndex++
				}
			}
		}

		return params
	}

	private fun parseTemplate(node: WtTemplate): CraftingRecipe {
		val params = extractParameters(node)


		val isShapeless = params["shapeless"] == "1"
		val showName = params["showname"] != "0"
		val showDescription = params["showdescription"] == "1"

		// Handle positional parameters - they represent slots in order
		// For a 2-slot shapeless recipe (like repair), pos1 and pos2 are the two input slots
		// Check if ANY named grid position exists (A1-C3)
		val hasNamedGrid = listOf("A1", "B1", "C1", "A2", "B2", "C2", "A3", "B3", "C3")
			.any { params.containsKey(it) }

		val grid = if (hasNamedGrid) {
			// Standard named grid parameters
			CraftingGrid(
				a1 = parseSlot(params["A1"]),
				b1 = parseSlot(params["B1"]),
				c1 = parseSlot(params["C1"]),
				a2 = parseSlot(params["A2"]),
				b2 = parseSlot(params["B2"]),
				c2 = parseSlot(params["C2"]),
				a3 = parseSlot(params["A3"]),
				b3 = parseSlot(params["B3"]),
				c3 = parseSlot(params["C3"])
			)
		} else {
			// Positional parameters - treat as shapeless recipe with slots
			CraftingGrid(
				a1 = parseSlot(params["pos1"]),
				b1 = parseSlot(params["pos2"]),
				c1 = parseSlot(params["pos3"]),
				a2 = parseSlot(params["pos4"]),
				b2 = parseSlot(params["pos5"]),
				c2 = parseSlot(params["pos6"]),
				a3 = parseSlot(params["pos7"]),
				b3 = parseSlot(params["pos8"]),
				c3 = parseSlot(params["pos9"])
			)
		}

		return CraftingRecipe(
			name = params["name"],
			ingredients = params["ingredients"],
			description = params["description"],
			grid = grid,
			output = parseSlot(params["Output"]),
			type = params["type"],
			isShapeless = isShapeless,
			showName = showName,
			showDescription = showDescription
		)
	}

	private fun parseSlot(slotValue: String?): SlotContent? {
		if (slotValue.isNullOrBlank()) return null

		val items = slotValue.split(";")
			.map { it.trim() }
			.filter { it.isNotEmpty() }
			.map { cleanItemName(it) }

		if (items.isEmpty()) return null

		return SlotContent(items)
	}

	private fun cleanItemName(name: String): String {
		var cleaned = name.replace(Regex("\\[\\[([^]|]+)(?:\\|[^]]+)?]]"), "$1")

		cleaned = cleaned.replace(Regex("[{}]"), "")

		val countMatch = Regex("([^,]+),\\s*(\\d+)").find(cleaned)
		if (countMatch != null) {
			cleaned = "${countMatch.groupValues[1]} (x${countMatch.groupValues[2]})"
		}

		return cleaned.trim()
	}

	private fun convertToBuilder(recipe: CraftingRecipe, builder: MarkdownDocumentBuilder) {
		if (recipe.showName && recipe.name != null) {
			builder.bold(cleanItemName(recipe.name))
			builder.blankLines(2)
		}

		if (recipe.ingredients != null) {
			builder.text("Ingredients: ${cleanItemName(recipe.ingredients)}")
			builder.blankLines(2)
		}

		val recipeTitle = buildString {
			append("Crafting Recipe")
			// Show the output in the title instead of the type
			if (recipe.output != null) {
				append(": ${recipe.output.format()}")
			}
			if (recipe.isShapeless) {
				append(" (Shapeless)")
			}
			append(":")
		}
		builder.text(recipeTitle)
		builder.blankLines(1)

		if (recipe.grid.isEmpty()) {
			if (recipe.ingredients != null) {
				builder.text("Uses: ${cleanItemName(recipe.ingredients)}")
				builder.blankLines(1)
			}
		} else {
			addGridToBuilder(recipe.grid, recipe.isShapeless, builder)
		}

		if (recipe.showDescription && recipe.description != null) {
			builder.blankLines(2)
			builder.text(recipe.description)
		}
	}

	private fun addGridToBuilder(grid: CraftingGrid, isShapeless: Boolean, builder: MarkdownDocumentBuilder) {
		if (isShapeless) {
			val ingredients = grid.getAllSlots()
				.filterNotNull()
				.map { it.format() }

			builder.text("Ingredients (shapeless): " + ingredients.joinToString(", "))
			builder.blankLines(1)
			return
		}

		// For shaped recipes, create a symbol mapping and show with legend
		val rows = listOf(
			listOf(grid.a1, grid.b1, grid.c1),
			listOf(grid.a2, grid.b2, grid.c2),
			listOf(grid.a3, grid.b3, grid.c3)
		)

		// Build a mapping of unique items to symbols
		val uniqueItems = rows.flatten()
			.filterNotNull()
			.map { it.format() }
			.distinct()

		// Create symbol map using first character of material names
		val symbolMap = mutableMapOf<String, Char>()
		val usedSymbols = mutableSetOf<Char>()

		for (item in uniqueItems) {
			val symbol = findAvailableSymbol(item, usedSymbols)
			symbolMap[item] = symbol
			usedSymbols.add(symbol)
		}

		val legend = symbolMap.entries.sortedBy { it.value }.joinToString(", ") { (item, symbol) ->
			"$symbol: $item"
		}

		builder.text("Grid ($legend):")
		builder.blankLines(1)

		// Display the grid as a raw block for better formatting
		val gridText = buildString {
			for (row in rows) {
				val cells = row.map { slot ->
					if (slot == null) {
						"_"
					} else {
						symbolMap[slot.format()]?.toString() ?: "?"
					}
				}
				appendLine(cells.joinToString(","))
			}
		}
		builder.rawBlock(gridText)
	}

	/**
	 * Find an available symbol for a material name.
	 * Tries first character, then first character of subsequent words, then any uppercase letter.
	 */
	private fun findAvailableSymbol(itemName: String, usedSymbols: Set<Char>): Char {
		// Try first character (uppercase)
		val firstChar = itemName.firstOrNull()?.uppercaseChar()
		if (firstChar != null && firstChar.isLetter() && firstChar !in usedSymbols) {
			return firstChar
		}

		// Try first character of each word
		val words = itemName.split(" ", "-")
		for (word in words.drop(1)) { // Skip first word since we already tried it
			val wordFirstChar = word.firstOrNull()?.uppercaseChar()
			if (wordFirstChar != null && wordFirstChar.isLetter() && wordFirstChar !in usedSymbols) {
				return wordFirstChar
			}
		}

		// Fallback: try any uppercase letter
		for (char in 'X'..'Z') {
			if (char !in usedSymbols) {
				return char
			}
		}

		// Ultimate fallback: use numbers
		for (char in '0'..'9') {
			if (char !in usedSymbols) {
				return char
			}
		}

		// If all else fails, use '?'
		return '?'
	}
}
