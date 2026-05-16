package se.ade.mc.wiki.md.main.nodes

import org.sweble.wikitext.parser.nodes.WtTemplate
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtName
import se.ade.mc.wiki.md.MarkdownDocumentBuilder
import java.util.Locale

/**
 * WikiText template handler for mob drop tables.
 * Parses {{DropTable}} with nested {{DropLine}} templates.
 * Filters to Java Edition only and outputs structured text.
 */
object DropTable {

	/**
	 * Represents a single drop line with all its properties
	 */
	data class DropLineData(
		val name: String,
		val quantityMin: Int?,
		val quantityMax: Int?,
		val lootingQuantityMin: Int?,
		val lootingQuantityMax: Int?,
		val dropChance: Double?,
		val lootingChanceIncrement: Double?,
		val notes: String?,
		val edition: String?
	) {
		val isRare: Boolean
			get() = dropChance != null
	}

	/**
	 * Main entry point for the DropTable template
	 */
	fun visit(node: WtTemplate, builder: MarkdownDocumentBuilder) {
		val dropLines = parseDropTable(node)

		if (dropLines.isEmpty()) {
			return
		}

		builder.paragraph {
			text("Drops:")
			blankLines(1)

			dropLines.forEach { dropLine ->
				val line = formatDropLine(dropLine)
				text("• $line")
				blankLines(1)
			}
		}
	}

	/**
	 * Parse the DropTable template and extract all DropLine entries
	 */
	private fun parseDropTable(node: WtTemplate): List<DropLineData> {
		val dropLines = mutableListOf<DropLineData>()

		// Iterate through all arguments looking for nested DropLine templates
		for (arg in node.args) {
			if (arg is WtTemplateArgument) {
				// Look for WtTemplate nodes in the argument value
				for (valueNode in arg.value) {
					if (valueNode is WtTemplate) {
						val templateName = valueNode.name.asString?.lowercase()?.trim()
						if (templateName == "dropline") {
							parseDropLine(valueNode)?.let { dropLines.add(it) }
						}
					}
				}
			}
		}

		return dropLines
	}

	/**
	 * Parse a single DropLine template
	 */
	private fun parseDropLine(template: WtTemplate): DropLineData? {
		val params = extractParameters(template)

		// Check edition filter - skip if bedrock, keep if java or unspecified
		val edition = params["edition"]?.lowercase()
		if (edition == "bedrock" || edition == "be") {
			return null
		}

		// Extract the item name
		val name = params["name"] ?: return null

		// Parse quantity (can be single number or range like "0-1")
		val quantity = params["quantity"]
		val (quantityMin, quantityMax) = parseQuantityRange(quantity)

		// Parse looting quantity
		val lootingQuantity = params["lootingquantity"]
		val (lootingQuantityMin, lootingQuantityMax) = parseQuantityRange(lootingQuantity)

		// Parse drop chance (for rare drops)
		val dropChance = params["dropchance"]?.toDoubleOrNull()

		// Parse looting chance increment (for rare drops)
		val lootingChance = params["lootingchance"]?.toDoubleOrNull()

		// Get notes (burn, notburn, player, etc.)
		val notes = params["notes"]

		return DropLineData(
			name = cleanItemName(name),
			quantityMin = quantityMin,
			quantityMax = quantityMax,
			lootingQuantityMin = lootingQuantityMin,
			lootingQuantityMax = lootingQuantityMax,
			dropChance = dropChance,
			lootingChanceIncrement = lootingChance,
			notes = notes,
			edition = edition
		)
	}

	/**
	 * Extract parameters from a template into a map
	 */
	private fun extractParameters(node: WtTemplate): Map<String, String> {
		val params = mutableMapOf<String, String>()

		for (arg in node.args) {
			if (arg is WtTemplateArgument) {
				// Get the argument name
				val argName = if (arg.name is WtName.WtNoName) {
					null
				} else {
					arg.name?.asString?.trim()?.lowercase()
				}

				// Extract value
				val parts = mutableListOf<String>()
				for (valueNode in arg.value) {
					when (valueNode) {
						is WtText -> parts.add(valueNode.content)
						else -> parts.add(valueNode.toString())
					}
				}

				val value = parts.joinToString("").trim()

				if (value.isNotEmpty() && argName != null && argName.isNotEmpty()) {
					params[argName] = value
				}
			}
		}

		return params
	}

	/**
	 * Parse a quantity string that can be a single number or a range like "0-1" or "-1-1"
	 * Returns (min, max) pair
	 */
	private fun parseQuantityRange(quantityStr: String?): Pair<Int?, Int?> {
		if (quantityStr.isNullOrBlank()) {
			return Pair(null, null)
		}

		// Handle range format like "0-1" or "-1-1"
		// Use regex to properly parse negative numbers in ranges
		val rangePattern = Regex("^(-?\\d+)-(-?\\d+)$")
		val match = rangePattern.find(quantityStr.trim())

		return if (match != null) {
			// It's a range
			val min = match.groupValues[1].toIntOrNull()
			val max = match.groupValues[2].toIntOrNull()
			Pair(min, max)
		} else {
			// Try to parse as a single number
			val num = quantityStr.trim().toIntOrNull()
			Pair(num, num)
		}
	}

	/**
	 * Clean item name by removing wiki markup
	 */
	private fun cleanItemName(name: String): String {
		var cleaned = name

		// Remove wiki links but keep the text: [[Page|Text]] -> Text, [[Page]] -> Page
		cleaned = cleaned.replace(Regex("\\[\\[([^]|]+)\\|([^]]+)]]"), "$2")
		cleaned = cleaned.replace(Regex("\\[\\[([^]]+)]]"), "$1")

		// Remove any remaining markup
		cleaned = cleaned.replace(Regex("[{}]"), "")

		return cleaned.trim()
	}

	/**
	 * Format a drop line into human-readable text
	 */
	private fun formatDropLine(drop: DropLineData): String {
		val parts = mutableListOf<String>()

		// Item name
		parts.add(drop.name)

		// Quantity or drop chance (rare drops)
		if (drop.isRare) {
			// Rare drop with percentage chance
			val baseChance = drop.dropChance!! * 100
			parts.add(formatRareDropChance(baseChance, drop.lootingChanceIncrement))
		} else {
			// Common/uncommon drop with quantity
			if (drop.quantityMin != null && drop.quantityMax != null) {
				// Check if this is an uncommon drop (negative minimum)
				if (drop.quantityMin < 0) {
					// Uncommon drop: -1 to 1 range means the drop has a probability
					// The range -1 to 1 has 3 possible values: -1, 0, 1
					// Only positive values are actual drops, so for -1 to 1, only "1" counts
					// That's 1 out of 3 possible values = 1/3 chance
					val range = drop.quantityMax - drop.quantityMin + 1
					val effectiveMax = drop.quantityMax

					// Calculate probability: only positive values count as drops
					val dropValues = if (effectiveMax > 0) effectiveMax else 0
					val chancePercent = (dropValues.toDouble() / range * 100)

					if (effectiveMax > 0) {
						parts.add("×0–$effectiveMax (${formatPercentage(chancePercent)} chance)")
					} else {
						parts.add("×0 (${formatPercentage(chancePercent)} chance)")
					}

					// Add looting bonus if present
					if (drop.lootingQuantityMin != null && drop.lootingQuantityMax != null) {
						val lootingText = formatLootingQuantity(drop.lootingQuantityMin, drop.lootingQuantityMax)
						parts.add(lootingText)
					}
				} else {
					// Regular common drop
					val quantityText = formatQuantityRange(drop.quantityMin, drop.quantityMax)
					parts.add(quantityText)

					// Add looting bonus if present
					if (drop.lootingQuantityMin != null && drop.lootingQuantityMax != null) {
						val lootingText = formatLootingQuantity(drop.lootingQuantityMin, drop.lootingQuantityMax)
						parts.add(lootingText)
					}
				}
			}
		}

		// Add conditional notes
		drop.notes?.let { notes ->
			val noteText = formatNotes(notes)
			if (noteText.isNotEmpty()) {
				parts.add("($noteText)")
			}
		}

		return parts.joinToString(" ")
	}

	/**
	 * Format quantity range like "1-3" or "1" if min equals max
	 */
	private fun formatQuantityRange(min: Int, max: Int): String {
		return if (min == max) {
			"×$min"
		} else {
			"×$min–$max"
		}
	}

	/**
	 * Format looting quantity bonus
	 */
	private fun formatLootingQuantity(min: Int, max: Int): String {
		return if (min == max) {
			"(Looting: +$min per level)"
		} else {
			"(Looting: +$min–$max per level)"
		}
	}

	/**
	 * Format rare drop chance with looting increments
	 * Example: "10% base chance, 13% with Looting I, 16% with Looting II, 19% with Looting III"
	 */
	private fun formatRareDropChance(baseChance: Double, lootingIncrement: Double?): String {
		val basePct = formatPercentage(baseChance)

		if (lootingIncrement == null || lootingIncrement == 0.0) {
			return "$basePct chance"
		}

		val lootingIncrementPct = lootingIncrement * 100

		val looting1 = baseChance + lootingIncrementPct
		val looting2 = baseChance + (lootingIncrementPct * 2)
		val looting3 = baseChance + (lootingIncrementPct * 3)

		return "$basePct base chance, ${formatPercentage(looting1)} with Looting I, " +
				"${formatPercentage(looting2)} with Looting II, ${formatPercentage(looting3)} with Looting III"
	}

	/**
	 * Format a percentage value, removing unnecessary decimals
	 */
	private fun formatPercentage(value: Double): String {
		return if (value % 1.0 == 0.0) {
			"${value.toInt()}%"
		} else {
			String.format(Locale.ROOT, "%.1f%%", value)
		}
	}

	/**
	 * Format notes into human-readable conditions
	 */
	private fun formatNotes(notes: String): String {
		return when (notes.lowercase()) {
			"burn" -> "only when on fire or killed with a weapon enchanted with Fire Aspect"
			"notburn" -> "if not burned"
			"player" -> "player kill required"
			else -> notes
		}
	}
}

