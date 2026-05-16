package se.ade.mc.wiki.md

/**
 * Renderer that converts the markdown AST to a formatted string
 */
class MarkdownRenderer(private val wrapCol: Int = 0) {
	private val content = StringBuilder()
	private val noWrap = wrapCol == 0

	private val currentLine = StringBuilder()
	private var needNewlines = 0
	private var needSpace = false
	private var pastBod = false // Past Beginning Of Document

	fun render(node: MarkdownNode): String {
		renderNode(node)
		finishLine()
		return content.toString()
	}

	fun render(nodes: List<MarkdownNode>): String {
		nodes.forEach { renderNode(it) }
		finishLine()
		return content.toString()
	}

	private fun renderNode(node: MarkdownNode) {
		when (node) {
			is MarkdownNode.Text -> writeText(node.content)
			is MarkdownNode.Section -> {
				// Render the heading
				finishLine()
				ensureBlankLines(2)
				content.append("#".repeat(node.level.coerceIn(1, 6)))
				content.append(" ")
				content.append(node.title.trim())
				content.append("\n")
				needNewlines = 0
				needSpace = false
				pastBod = true

				// Render the children (content and subsections)
				node.children.forEach { renderNode(it) }
			}
			is MarkdownNode.Heading -> {
				finishLine()
				ensureBlankLines(2)
				content.append("#".repeat(node.level.coerceIn(1, 6)))
				content.append(" ")
				content.append(node.content.trim())
				content.append("\n")
				needNewlines = 0
				needSpace = false
				pastBod = true
			}
			is MarkdownNode.Paragraph -> {
				node.children.forEach { renderNode(it) }
				requestBlankLines(2)
			}
			is MarkdownNode.Block -> {
				node.children.forEach { renderNode(it) }
			}
			is MarkdownNode.Bold -> {
				write("**")
				write(node.content)
				write("**")
			}
			is MarkdownNode.Italic -> {
				write("//")
				write(node.content)
				write("//")
			}
			is MarkdownNode.CodeBlock -> {
				finishLine()
				ensureBlankLines(1)
				content.append("```")
				content.append(node.language)
				content.append("\n")
				content.append(node.code.trimEnd())
				if (!node.code.endsWith("\n")) {
					content.append("\n")
				}
				content.append("```\n")
				ensureBlankLines(1)
			}
			is MarkdownNode.RawBlock -> {
				finishLine()
				ensureBlankLines(1)
				content.append(node.content)
				if (!node.content.endsWith("\n")) {
					content.append("\n")
				}
				ensureBlankLines(1)
			}
			is MarkdownNode.Table -> {
				finishLine()
				ensureBlankLines(2)
				content.append(renderTable(node))
				content.append("\n")
				needNewlines = 0
				needSpace = false
				pastBod = true
			}
			is MarkdownNode.UnorderedList -> {
				finishLine()
				ensureBlankLines(1)
				renderList(node.items, isOrdered = false, depth = 0)
				ensureBlankLines(1)
			}
			is MarkdownNode.OrderedList -> {
				finishLine()
				ensureBlankLines(1)
				renderList(node.items, isOrdered = true, depth = 0)
				ensureBlankLines(1)
			}
			is MarkdownNode.ListItem -> {
				// List items should only be rendered as part of a list
				// If encountered standalone, render their children
				node.children.forEach { renderNode(it) }
			}
			is MarkdownNode.HorizontalRule -> {
				finishLine()
				ensureBlankLines(1)
				content.append("---")
				content.append("\n")
				ensureBlankLines(2)
			}
			is MarkdownNode.LineBreak -> {
				finishLine()
				ensureBlankLines(1)
			}
			is MarkdownNode.BlankLines -> {
				requestBlankLines(node.count)
			}
			is MarkdownNode.Space -> {
				requestSpace()
			}
			is MarkdownNode.TemplateRedacted -> {
				write("{{${node.templateName}}}")
			}
		}
	}

	private fun renderTable(table: MarkdownNode.Table): String {
		val columnWidths = calculateColumnWidths(table.rows)
		val sb = StringBuilder()
		val headerSeparator = buildSeparator(columnWidths)

		for ((i, row) in table.rows.withIndex()) {
			sb.append(buildRow(row, columnWidths))
			if (i == table.headerRowIndex) {
				sb.append(headerSeparator)
			}
		}
		return sb.toString()
	}

	private fun calculateColumnWidths(rows: List<List<String>>): List<Int> {
		val maxColumns = rows.maxOfOrNull { it.size } ?: 0
		val widths = MutableList(maxColumns) { 0 }

		for (row in rows) {
			for ((i, cell) in row.withIndex()) {
				widths[i] = maxOf(widths[i], cell.length)
			}
		}

		return widths
	}

	private fun buildRow(cells: List<String>, columnWidths: List<Int>): String {
		val paddedCells = cells.mapIndexed { i, cell ->
			cell.padEnd(columnWidths.getOrElse(i) { 0 })
		}
		return "| " + paddedCells.joinToString(" | ") + " |\n"
	}

	private fun buildSeparator(columnWidths: List<Int>): String {
		val separators = columnWidths.map { "-".repeat(it + 2) }
		return "|${separators.joinToString("|")}|\n"
	}

	private fun writeText(text: String) {
		if (text.isEmpty()) return

		if (Character.isSpaceChar(text.first())) {
			requestSpace()
		}

		val words = text.split(Regex("\\s+"))
		words.forEachIndexed { index, word ->
			if (word.isNotEmpty()) {
				writeWord(word)
				if (index < words.size - 1) {
					requestSpace()
				}
			}
		}

		if (Character.isSpaceChar(text.last())) {
			requestSpace()
		}
	}

	private fun write(text: String) {
		if (text.isEmpty()) return

		if (Character.isSpaceChar(text.first())) {
			requestSpace()
		}

		val words = text.split(Regex("\\s+"))
		words.forEachIndexed { index, word ->
			if (word.isNotEmpty()) {
				writeWord(word)
				if (index < words.size - 1) {
					requestSpace()
				}
			}
		}

		if (text.isNotEmpty() && Character.isSpaceChar(text.last())) {
			requestSpace()
		}
	}

	private fun writeWord(word: String) {
		if (word.isEmpty()) return

		var length = word.length
		if (!noWrap && needNewlines <= 0) {
			if (needSpace) length += 1

			if (currentLine.length + length >= wrapCol && currentLine.length > 0) {
				ensureBlankLines(1)
			}
		}

		if (needSpace && needNewlines <= 0) {
			currentLine.append(' ')
		}

		if (needNewlines > 0) {
			ensureBlankLines(needNewlines)
		}

		needSpace = false
		pastBod = true
		currentLine.append(word)
	}

	private fun finishLine() {
		if (currentLine.isNotEmpty()) {
			content.append(currentLine)
			currentLine.setLength(0)
		}
	}

	private fun ensureBlankLines(count: Int) {
		finishLine()
		content.append("\n".repeat(count))
		needNewlines = 0
		needSpace = false
	}

	private fun requestBlankLines(count: Int) {
		if (pastBod && count > needNewlines) {
			needNewlines = count
		}
	}

	private fun requestSpace() {
		if (pastBod) {
			needSpace = true
		}
	}

	/**
	 * Render a list with proper indentation for nested lists
	 */
	private fun renderList(items: List<MarkdownNode.ListItem>, isOrdered: Boolean, depth: Int) {
		val indent = "  ".repeat(depth) // 2 spaces per depth level

		items.forEachIndexed { index, item ->
			// Determine the list marker
			val marker = if (isOrdered) {
				"${index + 1}."
			} else {
				"-"
			}

			// Write the indentation and marker
			content.append(indent)
			content.append(marker)
			content.append(" ")

			// Render item content inline using a helper
			val itemText = renderInlineContent(item)
			content.append(itemText.trim())
			content.append("\n")

			// Render nested lists with increased depth
			item.children.forEach { child ->
				when (child) {
					is MarkdownNode.UnorderedList -> {
						renderList(child.items, isOrdered = false, depth = depth + 1)
					}
					is MarkdownNode.OrderedList -> {
						renderList(child.items, isOrdered = true, depth = depth + 1)
					}
					else -> {}
				}
			}
		}

		// Reset state after list
		needNewlines = 0
		needSpace = false
		pastBod = true
	}

	// Helper to render inline content of a list item into a StringBuilder
	private fun renderInlineContent(item: MarkdownNode.ListItem): String {
		val sb = StringBuilder()
		val savedNeedNewlines = needNewlines
		val savedNeedSpace = needSpace
		val savedPastBod = pastBod

		// Only render non-list children
		item.children.forEach { child ->
			if (child !is MarkdownNode.UnorderedList && child !is MarkdownNode.OrderedList) {
				// Temporarily render into sb
				val oldContent = content
				val oldCurrentLine = currentLine
				val tempContent = sb
				val tempCurrentLine = StringBuilder()
				// Use reflection to set private vals (not ideal, but avoids var)
				val contentField = this::class.java.getDeclaredField("content")
				val currentLineField = this::class.java.getDeclaredField("currentLine")
				contentField.isAccessible = true
				currentLineField.isAccessible = true
				contentField.set(this, tempContent)
				currentLineField.set(this, tempCurrentLine)
				renderNode(child)
				finishLine()
				contentField.set(this, oldContent)
				currentLineField.set(this, oldCurrentLine)
			}
		}
		needNewlines = savedNeedNewlines
		needSpace = savedNeedSpace
		pastBod = savedPastBod
		return sb.toString()
	}
}