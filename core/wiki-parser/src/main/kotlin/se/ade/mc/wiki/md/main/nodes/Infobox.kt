package se.ade.mc.wiki.md.main.nodes

import org.sweble.wikitext.parser.nodes.WtTemplate
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtText
import se.ade.mc.wiki.md.MarkdownDocumentBuilder

/**
 * Parser for Minecraft Wiki infobox templates.
 * Handles complex infobox structures with groups, images, and multiple data rows.
 *
 * Based on the Lua implementation in Module:Infobox
 */
class Infobox(private val templateVisitor: TemplateVisitor) {

	/**
	 * Parse an infobox template and add it to the markdown builder
	 */
	fun visit(template: WtTemplate, builder: MarkdownDocumentBuilder) {
		val params = extractParameters(template)

		// Determine infobox type from template name
		// Templates can be in format "{{Infobox entity}}" or "{{Entity}}"
		val templateName = template.name.asString?.lowercase() ?: ""
		val infoboxType = when {
			templateName.contains("entity") -> InfoboxType.ENTITY
			templateName.contains("item") -> InfoboxType.ITEM
			templateName.contains("block") -> InfoboxType.BLOCK
			templateName.contains("biome") -> InfoboxType.BIOME
			templateName.startsWith("infobox") -> InfoboxType.GENERIC
			else -> InfoboxType.GENERIC
		}

		val infoboxData = parseInfobox(params, infoboxType)

		if (infoboxData.isEmpty()) return

		// Build the infobox as a structured text block
		builder.paragraph {
			text("--- INFOBOX (${infoboxType.name}) ---")
			blankLines(1)

			infoboxData.forEach { (key, value) ->
				if (value.isNotBlank()) {
					text("$key: $value")
					blankLines(1)
				}
			}

			text("--- END INFOBOX ---")
		}
	}

	private enum class InfoboxType {
		ITEM,
		ENTITY,
		BLOCK,
		BIOME,
		GENERIC
	}

	/**
	 * Extract all parameters from the template
	 */
	private fun extractParameters(template: WtTemplate): Map<String, String> {
		val params = mutableMapOf<String, String>()

		for (arg in template.args) {
			if (arg is WtTemplateArgument) {
				val value = extractArgumentValue(arg)
				if (value.isEmpty()) continue

				if (arg.hasName()) {
					val name = (arg.name.get(0) as? WtText)?.content?.trim()
					if (name != null) {
						params[name] = value
					}
				}
			}
		}

		return params
	}

	/**
	 * Extract the value from a template argument, handling nested templates
	 */
	private fun extractArgumentValue(arg: WtTemplateArgument): String {
		val parts = mutableListOf<String>()

		for (node in arg.value) {
			when (node) {
				is WtText -> {
					val text = node.content
					if (text.isNotBlank()) {
						parts.add(text)
					}
				}
				is WtTemplate -> {
					// Handle nested templates like {{ItemSprite|...}} and {{hp|...}}
					templateVisitor.parseToStringOrNull(node)?.let {
						if (it.isNotBlank()) {
							parts.add(it)
						}
					}
				}
				else -> {
					// Try to extract text from other node types
					val text = node.toString()
					if (text.isNotBlank()) {
						parts.add(text)
					}
				}
			}
		}

		// Join with space to prevent concatenation issues
		return parts.joinToString(" ").trim()
	}

	/**
	 * Parse infobox parameters into a structured format
	 */
	private fun parseInfobox(params: Map<String, String>, type: InfoboxType): Map<String, String> {
		val result = mutableMapOf<String, String>()

		// Extract title
		params["title"]?.let {
			if (it.isNotBlank()) {
				result["Title"] = formatValue(it)
			}
		}

		// Extract subtitle if present
		params["subtitle"]?.let { subtitle ->
			if (subtitle.isNotBlank()) {
				result["Subtitle"] = formatValue(subtitle)
			}
		}

		// Extract groups and their information (for ITEM type)
		if (type == InfoboxType.ITEM || type == InfoboxType.GENERIC) {
			val groups = extractGroups(params)
			if (groups.isNotEmpty()) {
				result["Variants"] = groups.joinToString(", ")
			}
		}

		// Extract extratext field
		params["extratext"]?.let { extratext ->
			if (extratext.isNotBlank() && extratext != "none") {
				result["Extra Info"] = formatValue(extratext)
			}
		}

		// Parse the rows parameter if present (contains structured infobox data)
		params["rows"]?.let { rows ->
			val parsedRows = parseRows(rows)
			parsedRows.forEach { (label, field) ->
				result[label] = field
			}
		}

		// Extract type-specific fields
		when (type) {
			InfoboxType.ITEM -> extractItemFields(params, result)
			InfoboxType.ENTITY -> extractEntityFields(params, result)
			InfoboxType.BLOCK -> extractBlockFields(params, result)
			InfoboxType.BIOME -> extractBiomeFields(params, result)
			InfoboxType.GENERIC -> {
				// Try all sets of fields for generic infoboxes
				extractItemFields(params, result)
				extractEntityFields(params, result)
				extractBlockFields(params, result)
				extractBiomeFields(params, result)
			}
		}

		// Extract footer
		params["footer"]?.let { footer ->
			if (footer.isNotBlank()) {
				result["Note"] = formatValue(footer)
			}
		}

		return result
	}

	/**
	 * Extract Item-specific fields
	 */
	private fun extractItemFields(params: Map<String, String>, result: MutableMap<String, String>) {
		extractStandardField(params, "rarity", "Rarity", result)
		extractStandardField(params, "attackdamage", "Attack Damage", result)
		extractStandardField(params, "attackspeed", "Attack Speed", result)
		extractStandardField(params, "armor", "Armor", result)
		extractStandardField(params, "armortoughness", "Armor Toughness", result)
		extractStandardField(params, "durability", "Durability", result)
		extractStandardField(params, "enchantability", "Enchantability", result)
		extractStandardField(params, "knockbackresistance", "Knockback Resistance", result)
		extractStandardField(params, "renewable", "Renewable", result)
		extractStandardField(params, "stackable", "Stackable", result)
		extractStandardField(params, "heals", "Heals", result)
		extractStandardField(params, "effects", "Effects", result)
	}

	/**
	 * Extract Block-specific fields
	 */
	private fun extractBlockFields(params: Map<String, String>, result: MutableMap<String, String>) {
		extractStandardField(params, "rarity", "Rarity", result)
		extractStandardField(params, "renewable", "Renewable", result)
		extractStandardField(params, "stackable", "Stackable", result)
		extractStandardField(params, "maxStack", "Maximum Stack", result)
		extractStandardField(params, "tool", "Tool", result)
		extractStandardField(params, "tool2", "Tool 2", result)
		extractStandardField(params, "tool3", "Tool 3", result)
		extractStandardField(params, "tntres", "Blast Resistance", result)
		extractStandardField(params, "hardness", "Hardness", result)
		extractStandardField(params, "durability", "Durability", result)
		extractStandardField(params, "light", "Emits Light", result)
		extractStandardField(params, "lightLevel", "Light Level", result)
		extractStandardField(params, "transparent", "Transparent", result)
		extractStandardField(params, "waterloggable", "Waterloggable", result)
		extractStandardField(params, "flammable", "Flammable", result)
		extractStandardField(params, "lavasusceptible", "Lava Susceptible", result)
		extractStandardField(params, "mapcolor", "Map Color", result)
		extractStandardField(params, "heals", "Heals", result)
	}

	/**
	 * Extract Biome-specific fields
	 */
	private fun extractBiomeFields(params: Map<String, String>, result: MutableMap<String, String>) {
		extractStandardField(params, "structures", "Structures", result)
		extractStandardField(params, "features", "Features", result)
		extractStandardField(params, "blocks", "Blocks", result)
		extractStandardField(params, "temperature", "Temperature", result)
		extractStandardField(params, "downfall", "Downfall", result)
		extractStandardField(params, "precipitation", "Precipitation", result)
		extractStandardField(params, "snow_accumulation", "Snow Accumulation", result)
		extractStandardField(params, "skycolor", "Sky Color", result)
		extractStandardField(params, "fogcolor", "Fog Color", result)
		extractStandardField(params, "grasscolor", "Grass Color", result)
		extractStandardField(params, "foliagecolor", "Foliage Color", result)
		extractStandardField(params, "watercolor", "Water Color", result)
		extractStandardField(params, "underwaterfogcolor", "Underwater Fog Color", result)
	}

	/**
	 * Extract Entity-specific fields
	 */
	private fun extractEntityFields(params: Map<String, String>, result: MutableMap<String, String>) {
		extractStandardField(params, "addedVersion", "Added Version", result)
		extractStandardField(params, "addedDate", "Added Date", result)
		extractStandardField(params, "introducedEvent", "Introduced Event", result)
		extractStandardField(params, "introducedEventDate", "Introduced Event Date", result)
		extractStandardField(params, "removedVersion", "Removed Version", result)
		extractStandardField(params, "removedDate", "Removed Date", result)
		extractStandardField(params, "health", "Health", result)
		extractStandardField(params, "armor", "Armor", result)
		extractStandardField(params, "behavior", "Behavior", result)
		extractStandardField(params, "mobtype", "Mob Type", result)
		extractStandardField(params, "damage", "Damage", result)
		extractStandardField(params, "size", "Hitbox Size", result)
		extractStandardField(params, "speed", "Movement Speed", result)
		extractStandardField(params, "knockbackresistance", "Knockback Resistance", result)
		extractStandardField(params, "spawn", "Spawn", result)
		extractStandardField(params, "equipment", "Equipment", result)
		extractStandardField(params, "usableitems", "Usable Items", result)
		extractStandardField(params, "leashable", "Leashable", result)
		extractStandardField(params, "boat", "Can Enter Boats", result)
		extractStandardField(params, "minecart", "Can Enter Minecarts", result)
		extractStandardField(params, "networkid", "Network ID", result)
		extractStandardField(params, "rarity", "Rarity", result)
		extractStandardField(params, "tickingorder", "Ticking Order", result)
		extractStandardField(params, "gravity", "Gravity", result)
		extractStandardField(params, "drag", "Drag", result)
		extractStandardField(params, "drag_h", "Horizontal Drag", result)
		extractStandardField(params, "drag_v", "Vertical Drag", result)
		extractStandardField(params, "hurting_projectile", "Hurting Projectile", result)
		extractStandardField(params, "notes", "Notes", result)
	}

	/**
	 * Extract group information from numbered group parameters (group, group1, group2, ...)
	 */
	private fun extractGroups(params: Map<String, String>): List<String> {
		val groups = mutableListOf<String>()

		// Check for 'group' (equivalent to group1)
		params["group"]?.let {
			if (it.isNotBlank() && it.lowercase() != "none") {
				groups.add(formatValue(it))
			}
		}

		// Check for numbered groups (group2, group3, ...)
		var groupNum = 2
		while (true) {
			val groupName = params["group$groupNum"]
			if (groupName != null && groupName.isNotBlank() && groupName.lowercase() != "none") {
				groups.add(formatValue(groupName))
				groupNum++
			} else {
				break
			}
		}

		return groups
	}

	/**
	 * Extract image information from numbered image parameters
	 */
	private fun extractImages(params: Map<String, String>): List<String> {
		val images = mutableListOf<String>()

		// Check for 'image' (equivalent to image1)
		params["image"]?.let {
			if (it.isNotBlank() && it.lowercase() != "none" && it != "title") {
				images.add(formatValue(it))
			}
		}

		// Check for numbered images
		var imgNum = 1
		while (true) {
			val imageName = params["image$imgNum"]
			if (imageName != null && imageName.isNotBlank() &&
				imageName.lowercase() != "none" && imageName != "title") {
				images.add(formatValue(imageName))
				imgNum++
			} else if (imgNum > 10) {
				// Stop after checking a reasonable number
				break
			} else {
				imgNum++
			}
		}

		return images
	}

	/**
	 * Extract inventory image information from numbered invimage parameters
	 */
	private fun extractInvImages(params: Map<String, String>): List<String> {
		val invImages = mutableListOf<String>()

		// Check for 'invimage' (equivalent to invimage1)
		params["invimage"]?.let {
			if (it.isNotBlank() && it.lowercase() != "none" && it != "title" && it != "----") {
				invImages.add(formatValue(it))
			}
		}

		// Check for numbered invimages
		var imgNum = 1
		while (true) {
			val imageName = params["invimage$imgNum"]
			if (imageName != null && imageName.isNotBlank() &&
				imageName.lowercase() != "none" && imageName != "title" && imageName != "----") {
				invImages.add(formatValue(imageName))
				imgNum++
			} else if (imgNum > 10) {
				break
			} else {
				imgNum++
			}
		}

		return invImages
	}

	/**
	 * Extract a standard field and add it to the result if present
	 */
	private fun extractStandardField(
		params: Map<String, String>,
		paramKey: String,
		displayName: String,
		result: MutableMap<String, String>
	) {
		val value = params[paramKey]
		if (value != null && value.isNotBlank()) {
			result[displayName] = formatValue(value)
		}
	}

	/**
	 * Parse the structured rows parameter
	 * Format: <code class="history-json">{"label": "...", "field": "..."}</code>
	 */
	private fun parseRows(rows: String): List<Pair<String, String>> {
		val result = mutableListOf<Pair<String, String>>()

		// The rows parameter contains the actual structured data
		// It's formatted as wiki table syntax with labels and values
		// For now, we'll do basic extraction of label/value pairs

		// Try to extract from JSON-style format if present
		val jsonPattern = Regex("""["']label["']:\s*["']([^"']+)["'],\s*["']field["']:\s*["']([^"']+)["']""")
		jsonPattern.findAll(rows).forEach { match ->
			val label = formatValue(match.groupValues[1])
			val field = formatValue(match.groupValues[2])
			if (label.isNotBlank() && field.isNotBlank()) {
				result.add(label to field)
			}
		}

		// If no JSON format found, try wiki table format
		if (result.isEmpty()) {
			// Parse wiki table rows (! label || field format)
			val rowPattern = Regex("""!\s*([^|]+)\s*\|\|\s*(.+)""", RegexOption.MULTILINE)
			rowPattern.findAll(rows).forEach { match ->
				val label = formatValue(match.groupValues[1])
				val field = formatValue(match.groupValues[2])
				if (label.isNotBlank() && field.isNotBlank()) {
					result.add(label to field)
				}
			}
		}

		return result
	}

	/**
	 * Format a value for display - only handles display formatting, not parsing
	 */
	private fun formatValue(value: String): String {
		var formatted = value.trim()

		// Handle newlines - convert to semicolons for list items
		if (formatted.contains('\n')) {
			val lines = formatted.split('\n')
				.map { it.trim() }
				.filter { it.isNotEmpty() }
				.map { it.replace(Regex("^\\*\\s*"), "") } // Remove bullet points

			formatted = lines.joinToString("; ")
		}

		// Normalize whitespace
		formatted = formatted.replace(Regex("\\s+"), " ")

		return formatted.trim()
	}
}
