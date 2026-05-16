package se.ade.mc.wiki.md.main.nodes

import org.sweble.wikitext.parser.nodes.WtTemplate
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtInternalLink
import org.sweble.wikitext.parser.nodes.WtName
import org.sweble.wikitext.parser.nodes.WtNewline
import se.ade.mc.wiki.md.MarkdownDocumentBuilder
import se.ade.mc.wiki.md.WikiToMdVisitor

class TemplateVisitor(private val parent: WikiToMdVisitor, private val pageName: String) {
	fun parse(t: WtTemplate, builder: MarkdownDocumentBuilder) {
		val type = extractTemplateName(t) ?: return
		val typeId = type.lowercase().trim()

		// Check if this is an infobox template (route to dedicated parser)
		if (type.startsWith("Infobox", ignoreCase = true)) {
			Infobox(this).visit(t, builder)
			return
		}

		// Handle columns-list specially - it contains body content that needs to be rendered
		when (typeId) {
			"crafting" -> {
				Crafting.visit(t, builder)
				return
			}
			"droptable" -> {
				DropTable.visit(t, builder)
				return
			}
			"columns-list" -> {
				columnslist(t, builder)
				return
			}
			"for" -> {
				forTemplate(t, builder)
				return
			}
			else -> {
				val text = parseToStringOrNull(t)

				if (text != null) {
					builder.text(text)
				} else {
					// Template was not recognized, create a redacted marker
					val templateName = extractTemplateName(t) ?: "unknown"
					builder.templateRedacted(templateName)
				}
			}
		}
	}

	private fun xp(t: WtTemplate): String {
		val xp = t.args.get(0) as? WtTemplateArgument
			?: return ""

		when(val v = xp.value.get(0)) {
			is WtText -> return "${v.content} XP"
			else -> return ""
		}
	}

	/**
	 * Extract the template name from a WtTemplate, handling cases where the name
	 * may embed nested templates (e.g. {{ucfirst:{{lc:{{PAGENAME}}}}}} ).
	 * We evaluate nested templates so outer parser functions receive the already
	 * processed argument string.
	 */
	private fun extractTemplateName(t: WtTemplate): String? {
		val direct = try { t.name.asString } catch (_: IllegalStateException) { null }
		if (direct != null) return direct

		val parts = mutableListOf<String>()

		for (node in t.name) {
			when (node) {
				is WtText -> parts.add(node.content)
				is WtTemplate -> {
					// Try full evaluation of the nested template
					val evaluated = parseToStringOrNull(node)
					if (evaluated != null) {
						parts.add(evaluated)
					} else {
						// Fallback for simple magic word
						val nestedName = try { node.name.asString?.trim()?.uppercase() } catch (_: IllegalStateException) { null }
						if (nestedName == "PAGENAME") parts.add(pageName)
					}
				}
			}
		}

		return parts.takeIf { it.isNotEmpty() }?.joinToString("")
	}

	/**
	 * Handle parser functions like {{lc:text}}, {{uc:text}}, etc.
	 * Parser functions use colon syntax and can have nested templates as arguments
	 */
	private fun handleParserFunction(t: WtTemplate, fullName: String): String? {
		val colonIndex = fullName.indexOf(':')
		if (colonIndex == -1) return null

		val functionName = fullName.substring(0, colonIndex).lowercase()
		val directArgument = fullName.substring(colonIndex + 1).trim()

		// Get the argument - either from the function name itself or from template args
		val argument = if (directArgument.isNotEmpty()) {
			directArgument
		} else {
			// Check if there's a nested template or argument
			val arg = t.args.firstOrNull() as? WtTemplateArgument
			if (arg != null) {
				// Extract the argument value, handling nested templates
				extractArgumentValue(arg)
			} else {
				null
			}
		} ?: return null

		// Apply the parser function
		return when (functionName) {
			"lc", "lcase", "lowercase" -> argument.lowercase()
			"uc", "ucase", "uppercase" -> argument.uppercase()
			"lcfirst" -> argument.replaceFirstChar { it.lowercase() }
			"ucfirst" -> argument.replaceFirstChar { it.uppercase() }
			else -> null // Unknown parser function
		}
	}

	/**
	 * Extract the value from a template argument, handling nested templates and magic words
	 */
	private fun extractArgumentValue(arg: WtTemplateArgument): String? {
		val parts = mutableListOf<String>()

		for (node in arg.value) {
			when (node) {
				is WtText -> parts.add(node.content)
				is WtTemplate -> {
					// Handle nested templates - check for magic words first
					val nestedName = node.name.asString?.trim()?.uppercase()
					if (nestedName == "PAGENAME") {
						parts.add(pageName)
					} else {
						// Try to parse as a regular template
						parseToStringOrNull(node)?.let { parts.add(it) }
					}
				}
			}
		}

		return if (parts.isNotEmpty()) parts.joinToString("") else null
	}


	// Internal method for use by TableToText when extracting cell content
	internal fun parseToStringOrNull(t: WtTemplate): String? {
		val type = extractTemplateName(t) ?: return null

		// Handle parser functions (those with colons)
		if (type.contains(':')) {
			return handleParserFunction(t, type)
		}

		// Handle magic words
		if (type.uppercase() == "PAGENAME") {
			return pageName
		}

		return when(type) {
			"ItemLink" -> itemlink(t)
			"BlockLink" -> blockLink(t)
			"Exclusive" -> exclusive(t)
			"ItemSprite" -> itemSprite(t)
			"in" -> inEdition(t)
			"IN" -> inEdition(t)
			"JE" -> "(Java Edition)"
			"BE" -> "(Bedrock Edition)"
			"Hp" -> hp(t)
			"Xp" -> xp(t)
			"xp" -> xp(t)
			"experience" -> xp(t)
			"rewrite" -> rewrite(t)
			"improve lead" -> improveLead(t)
			"missing information" -> missingInformation(t)
			"empty section" -> emptySection(t)
			"incomplete section" -> incompleteSection(t)
			"more citations needed" -> moreCitationsNeeded(t)
			"stub" -> stub(t)
			"expand section" -> expandSection(t)
			"cleanup" -> cleanup(t)
			"too technical" -> tooTechnical(t)
			"Control" -> control(t)
			"Main" -> mainArticleReference(t)
			"About" -> about(t)
			"section link", "slink" -> slink(t)
			"frac" -> frac(t)
			"only" -> only(t)
			"Education feature" -> "This page describes an education-related feature. This feature is available only in Minecraft Education or when enabling the \"Minecraft Education features\" cheat setting in Bedrock Edition."
			else -> null
		}
	}

	/**
	 * Disambiguation "for" template
	 * {{For|similar creatures in the [[Minecraft franchise|''Minecraft'' franchise]]|Enderman (disambiguation)}}--- INFOBOX
	 */
	private fun forTemplate(t: WtTemplate, builder: MarkdownDocumentBuilder) {
		val args = t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name is WtName.WtNoName || it.name.isNullOrEmpty() } // Only positional args
			.mapNotNull { (it.value.getOrNull(0) as? WtText)?.content }

		if (args.size < 2) return

		// First arg is the subject of disambiguation
		val subject = args[0]?.let { cleanWikiMarkup(it) } ?: return
		val link = args[1]?.let { cleanWikiMarkup(it) } ?: return

		builder.paragraph {
			text("For $subject, see [[$link]].")
		}
	}

	/**
	 * Disambiguation notice template
	 * {{About|the weapon in ''[[Minecraft]]''|other namesakes|Axe (disambiguation)}}
	 *
	 * Format: {{About|subject|other use description|other page link}}
	 * Can have multiple pairs of use description + link
	 */
	private fun about(t: WtTemplate): String? {
		val args = t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name.isNullOrEmpty() } // Only positional args
			.mapNotNull { (it.value.getOrNull(0) as? WtText)?.content }

		if (args.isEmpty()) return null

		// First arg is the subject of this article
		val subject = args.getOrNull(0)?.let { cleanWikiMarkup(it) } ?: return null

		// Remaining args come in pairs: description + link
		val otherUses = mutableListOf<Pair<String, String>>()
		var i = 1
		while (i < args.size - 1) {
			val description = cleanWikiMarkup(args[i])
			val link = cleanWikiMarkup(args[i + 1])
			otherUses.add(description to link)
			i += 2
		}

		return if (otherUses.isNotEmpty()) {
			val usesText = otherUses.joinToString(" and ") { (desc, link) ->
				"$desc, see [[$link]]"
			}
			"This article is about $subject. For $usesText."
		} else {
			"This article is about $subject."
		}
	}

	/**
	 * Clean up wiki markup from template arguments (remove extra quotes, wiki formatting)
	 */
	private fun cleanWikiMarkup(text: String): String {
		var cleaned = text.trim()
		// Remove wiki links but keep the text: [[Page|Text]] -> Text, [[Page]] -> Page
		cleaned = cleaned.replace(Regex("\\[\\[([^]|]+)\\|([^]]+)]]"), "$2")
		cleaned = cleaned.replace(Regex("\\[\\[([^]]+)]]"), "$1")
		// Remove italic markers ''text'' -> text
		cleaned = cleaned.replace(Regex("''([^']+)''"), "$1")
		// Remove bold markers '''text''' -> text
		cleaned = cleaned.replace(Regex("'''([^']+)'''"), "$1")
		return cleaned
	}

	/**
	 * "Main article: [[Article Name]]"
	 * e.g.
	 * {{main|Anvil mechanics#Unit repair}}
	 */
	private fun mainArticleReference(t: WtTemplate): String? {
		val arg = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = arg.value.get(0)) {
			is WtText -> return "(Main article: [[${v.content}]])"
			else -> return null
		}
	}

	private fun control(t: WtTemplate): String? {
		val arg = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = arg.value.get(0)) {
			is WtText -> return "(Button:${v.content?.uppercase()})"
			else -> return null
		}
	}

	/**
	 * Template that links to an item page.
	 * {{ItemLink|Diamond Sword}}
	 *
	 * Downconverts to regular wiki link:
	 * [[Diamond Sword]]
	 */
	private fun itemlink(t: WtTemplate): String? {
		val link = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = link.value.get(0)) {
			is WtText -> return "[[${v.content}]]"
			else -> return null
		}
	}

	private fun blockLink(t: WtTemplate): String? {
		val link = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = link.value.get(0)) {
			is WtText -> return "[[${v.content}]]"
			else -> return null
		}
	}

	private fun exclusive(t: WtTemplate): String? {
		val edition = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = edition.value.get(0)) {
			is WtText -> return "(This feature is exclusive to ${v.content} Edition.)"
			else -> return null
		}
	}

	/**
	 * Template that shows an item sprite.
	 * {{ItemSprite|Diamond Sword}}
	 *
	 * Downconverts to regular wiki link:
	 * [[Diamond Sword]]
	 */
	private fun itemSprite(t: WtTemplate): String? {
		val link = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = link.value.get(0)) {
			is WtText -> return "[[${v.content}]]"
			else -> return null
		}
	}

	/**
	 * Template that shows hit points.
	 * {{hp|20}}
	 *
	 * Convert to just the number:
	 * 20hp
	 */
	private fun hp(t: WtTemplate): String? {
		val hp = t.args.get(0) as? WtTemplateArgument
			?: return null

		when(val v = hp.value.get(0)) {
			is WtText -> return "${v.content}hp"
			else -> return null
		}
	}

	private fun inEdition(t: WtTemplate): String? {
		val editions = t.args
			.filterIsInstance<WtTemplateArgument>()
			.mapNotNull {
				val text = (it.value.firstOrNull() as? WtText)
					?.content
					?.lowercase()

				if (text != null && text in setOf("java", "je", "be", "bedrock", "console", "earth", "3ds")) {
					text
				} else {
					null
				}

			}

		return if (editions.isNotEmpty()) {
			val editionNames = editions.map { ed ->
				when (ed.lowercase()) {
					"java", "je" -> "Java Edition"
					"bedrock", "be" -> "Bedrock Edition"
					"console" -> "Console Edition"
					"earth" -> "Minecraft Earth"
					"3ds" -> "Nintendo 3DS Edition"
					else -> "$ed edition"
				}
			}

			val editionText = when (editionNames.size) {
				1 -> editionNames[0]
				2 -> "${editionNames[0]} and ${editionNames[1]}"
				else -> {
					val allButLast = editionNames.dropLast(1).joinToString(", ")
					"$allButLast and ${editionNames.last()}"
				}
			}

			"[$editionText only]"
		} else {
			null
		}
	}

	/**
	 * "(This section may be outdated or incorrect)" with reason
	 * {{rewrite|The combat mechanics have changed in version 1.9.}}
	 */
	fun rewrite(t: WtTemplate): String? {
		val args = t.args.filterIsInstance<WtTemplateArgument>()
			.filter {
				// Remove named args like section=, date= etc, the reason is plain text
				it.name.isNullOrEmpty()
			}
			.mapNotNull {
				(it.value.get(0) as? WtText)?.content
			}

		val reason = args.firstOrNull {
			!it.matches("^[A-Za-z]=.*$".toRegex())
		}

		return if(reason != null)
			"(Internal Note; this needs editing: $reason)"
		else
			"(This section may be outdated, incorrect or badly written.)"
	}

	/**
	 * Template for pages that need a better lead section
	 * {{improve lead|reason}}
	 */
	private fun improveLead(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; lead section needs improvement: $reason)"
		else
			"(This page needs a better lead section.)"
	}

	/**
	 * Template for pages that miss or don't have enough information
	 * {{missing information|reason}}
	 */
	private fun missingInformation(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; missing information: $reason)"
		else
			"(This page is missing information.)"
	}

	/**
	 * Template for sections that are empty
	 * {{empty section}}
	 */
	private fun emptySection(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; empty section: $reason)"
		else
			"(This section is empty.)"
	}

	/**
	 * Template for sections that need more information
	 * {{incomplete section|reason}}
	 */
	private fun incompleteSection(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; incomplete section: $reason)"
		else
			"(This section is incomplete.)"
	}

	/**
	 * Template for pages that need more references
	 * {{more citations needed|reason}}
	 */
	private fun moreCitationsNeeded(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; more citations needed: $reason)"
		else
			"(This page needs more citations.)"
	}

	/**
	 * Template for very short articles
	 * {{stub|reason}}
	 */
	private fun stub(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; stub article: $reason)"
		else
			"(This article is a stub.)"
	}

	/**
	 * Template for sections that need to be expanded
	 * {{expand section|reason}}
	 */
	private fun expandSection(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; section needs expansion: $reason)"
		else
			"(This section needs to be expanded.)"
	}

	/**
	 * Template for pages that do not conform to style guidelines
	 * {{cleanup|reason}}
	 */
	private fun cleanup(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; needs cleanup: $reason)"
		else
			"(This page needs cleanup to conform to style guidelines.)"
	}

	/**
	 * Template for pages that are too technical
	 * {{too technical|reason}}
	 */
	private fun tooTechnical(t: WtTemplate): String? {
		val args = extractPlainArguments(t)
		val reason = args.firstOrNull()

		return if (reason != null)
			"(Internal Note; too technical: $reason)"
		else
			"(This page may use technical jargon and be difficult to understand.)"
	}

	/**
	 * Helper function to extract plain text arguments (non-named parameters)
	 * Filters out named arguments and extracts text content
	 */
	private fun extractPlainArguments(t: WtTemplate): List<String> {
		return t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name.isNullOrEmpty() }
			.mapNotNull { (it.value.getOrNull(0) as? WtText)?.content }
			.filter { !it.matches("^[A-Za-z]+=.*$".toRegex()) }
	}

	private fun slink(t: WtTemplate): String? {
		val args = t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name.isNullOrEmpty() } // Only positional args

		return when (args.size) {
			1 -> {
				// Link to section in the same article
				val sectionText = (args[0].value.getOrNull(0) as? WtText)?.content ?: return null
				val section = cleanWikiMarkup(sectionText)
				"section: $section"
			}
			2 -> {
				// Link to section in another article
				val articleText = (args[0].value.getOrNull(0) as? WtText)?.content ?: ""
				val sectionText = (args[1].value.getOrNull(0) as? WtText)?.content ?: return null

				if (articleText.isEmpty()) {
					// Empty first parameter means same article
					val section = cleanWikiMarkup(sectionText)
					"section: $section"
				} else {
					val targetArticle = cleanWikiMarkup(articleText)
					val section = cleanWikiMarkup(sectionText)
					"'$section' in [[$targetArticle]]"
				}
			}
			else -> null
		}
	}

	/**
	 * Template for formatting fractions.
	 * {{frac|integer|numerator|denominator}} or {{frac|numerator|denominator}}
	 *
	 * Examples:
	 * {{frac|A|B}} -> A/B (numerator/denominator)
	 * {{frac|A|B|C}} -> A B/C (integer numerator/denominator)
	 */
	private fun frac(t: WtTemplate): String? {
		val args = t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name.isNullOrEmpty() } // Only positional args

		return when (args.size) {
			2 -> {
				// Simple fraction: {{frac|A|B}} -> A/B
				val numerator = (args[0].value.getOrNull(0) as? WtText)?.content ?: return null
				val denominator = (args[1].value.getOrNull(0) as? WtText)?.content ?: return null
				"$numerator/$denominator"
			}
			3 -> {
				// Fraction with integer: {{frac|A|B|C}} -> A B/C
				val integer = (args[0].value.getOrNull(0) as? WtText)?.content ?: return null
				val numerator = (args[1].value.getOrNull(0) as? WtText)?.content ?: return null
				val denominator = (args[2].value.getOrNull(0) as? WtText)?.content ?: return null
				"$integer $numerator/$denominator"
			}
			else -> null
		}
	}

	/**
	 * Template for edition exclusivity notes.
	 * {{only|java}} -> (Java Edition only)
	 * {{only|java|bedrock}} -> (Java Edition and Bedrock Edition only)
	 * {{only|java|bedrock|console}} -> (Java Edition, Bedrock Edition and Console Edition only)
	 *
	 * Can also handle named parameters like short=1, upcoming=, etc.
	 */
	private fun only(t: WtTemplate): String? {
		// Get all positional arguments (editions)
		val editions = t.args.filterIsInstance<WtTemplateArgument>()
			.filter { it.name.isNullOrEmpty() }
			.mapNotNull { (it.value.getOrNull(0) as? WtText)?.content }
			.map { normalizeEditionName(it) }
			.filter { it.isNotEmpty() }

		if (editions.isEmpty()) return null

		// Format the edition list
		val editionText = when (editions.size) {
			1 -> editions[0]
			2 -> "${editions[0]} and ${editions[1]}"
			else -> {
				val allButLast = editions.dropLast(1).joinToString(", ")
				"$allButLast and ${editions.last()}"
			}
		}

		return "($editionText only)"
	}

	/**
	 * Normalize edition names to full format
	 */
	private fun normalizeEditionName(edition: String): String {
		return when (edition.lowercase()) {
			"java", "je" -> "Java Edition"
			"bedrock", "be" -> "Bedrock Edition"
			"education" -> "Education Edition"
			"earth" -> "Minecraft Earth"
			"console" -> "Console Edition"
			"dungeons" -> "Minecraft Dungeons"
			"legends" -> "Minecraft Legends"
			"pocket", "pe" -> "Pocket Edition"
			else -> edition.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
		}
	}

	/**
	 * Columns-list template handler.
	 * {{columns-list|colwidth=20em|
	 * * [[Item 1]]
	 * * [[Item 2]]
	 * }}
	 *
	 * We ignore the column formatting and just pass through the content (typically a bulleted list).
	 * The content is in the last positional argument (after any named parameters like colwidth).
	 */
	private fun columnslist(t: WtTemplate, builder: MarkdownDocumentBuilder) {
		// Find the content argument - it's typically the last positional argument
		// Named arguments like "colwidth" are filtered out
		val contentArg = t.args.filterIsInstance<WtTemplateArgument>()
			.lastOrNull { it.name.isNullOrEmpty() }

		if (contentArg != null) {
			// Extract the text content from the argument
			// The content is typically a list, so we'll extract it as-is
			val content = extractTemplateArgumentContent(contentArg)
			if (content.isNotBlank()) {
				// Add the content as raw text - it's typically already formatted as a list
				builder.text(content.trim())
			}
		}
	}

	/**
	 * Extract content from a template argument, handling nested structures
	 */
	private fun extractTemplateArgumentContent(arg: WtTemplateArgument): String {
		val parts = mutableListOf<String>()

		for (node in arg.value) {
			when (node) {
				is WtText -> {
					parts.add(node.content)
				}
				is WtInternalLink -> {
					// Extract link text: [[Page]] or [[Page|Display]]
					val target = node.target.asString ?: ""
					parts.add("[[$target]]")
				}
				is WtNewline -> {
					parts.add("\n")
				}
				is WtTemplate -> {
					// Handle nested templates
					parseToStringOrNull(node)?.let {
						parts.add(it)
					}
				}
				// Handle other node types as needed
				else -> {
					// For other nodes, try to extract basic text
					if (node is WtText) {
						parts.add(node.content)
					}
				}
			}
		}

		return parts.joinToString("")
	}
}