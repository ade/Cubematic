package se.ade.mc.wiki.md.main.nodes

import de.fau.cs.osr.ptk.common.AstVisitor
import org.sweble.wikitext.parser.nodes.*
import se.ade.mc.wiki.md.MarkdownDocumentBuilder

class TableVisitor(private val templateVisitor: TemplateVisitor) : AstVisitor<WtNode>() {

	private data class CellSpan(val content: String, val remainingRows: Int)

	fun visit(table: WtTable, builder: MarkdownDocumentBuilder) {
		val rows = mutableListOf<List<String>>()
		val headerRowIndices = mutableListOf<Int>()

		val body = table.body
		if (body.isEmpty()) return

		processNode(body, rows, headerRowIndices)

		if (rows.isEmpty()) return

		// Use header row's column count as canonical
		val headerIndex = headerRowIndices.firstOrNull() ?: 0
		val canonicalColumns = rows.getOrNull(headerIndex)?.size ?: (rows.maxOfOrNull { it.size } ?: 0)
		val paddedRows = rows.map { row ->
			if (row.size < canonicalColumns) row + List(canonicalColumns - row.size) { "" }
			else if (row.size > canonicalColumns) row.take(canonicalColumns)
			else row
		}

		builder.table(paddedRows, headerIndex)
	}

	private fun processNode(node: WtNode, rows: MutableList<List<String>>, headerRowIndices: MutableList<Int>) {
		// Track cells that span multiple rows: map of (column index -> CellSpan)
		val rowSpans = mutableMapOf<Int, CellSpan>()
		val tableRows = mutableListOf<WtTableRow>()

		// Collect all rows from the table body, regardless of wrapper type
		fun collectRows(n: WtNode) {
			when (n) {
				is WtTableRow -> tableRows.add(n)
				is WtTableImplicitTableBody, is WtBody, is WtNodeList -> {
					iterate(n) { child -> collectRows(child) }
				}
				else -> {
					try {
						iterate(n) { child -> collectRows(child) }
					} catch (_: Exception) {}
				}
			}
		}

		collectRows(node)

		// Process each row with rowspan tracking
		for (tableRow in tableRows) {
			val cells = mutableListOf<String>()
			var isHeaderRow = true

			// Collect cells from this row (not yet positioned in columns)
			val cellsFromThisRow = mutableListOf<WtNode>()
			iterate(tableRow.body) { cell ->
				when (cell) {
					is WtTableHeader, is WtTableCell -> {
						cellsFromThisRow.add(cell)
						if (cell is WtTableCell) isHeaderRow = false
					}
				}
			}

			// Now build the row, inserting cells in the correct column positions
			var cellIndex = 0 // Index into cellsFromThisRow
			var columnIndex = 0 // Current column position in the output row

			// We need to determine how many columns total
			val maxColumn = (rowSpans.keys.maxOrNull() ?: -1) + 1
			val estimatedColumns = maxOf(maxColumn, cellsFromThisRow.size + rowSpans.size)

			// Process columns until we've placed all cells
			while (cellIndex < cellsFromThisRow.size || columnIndex < estimatedColumns) {
				// Check if this column has a spanning cell from a previous row
				val spanningCell = rowSpans[columnIndex]
				if (spanningCell != null) {
					cells.add(spanningCell.content)
					if (spanningCell.remainingRows > 1) {
						rowSpans[columnIndex] = spanningCell.copy(remainingRows = spanningCell.remainingRows - 1)
					} else {
						rowSpans.remove(columnIndex)
					}
					columnIndex++
					continue
				}

				// If no spanning cell, try to place a cell from this row
				if (cellIndex < cellsFromThisRow.size) {
					val cellNode = cellsFromThisRow[cellIndex]
					cellIndex++

					val cellContent = when (cellNode) {
						is WtTableHeader -> extractCellContent(cellNode.body)
						is WtTableCell -> extractCellContent(cellNode.body)
						else -> ""
					}

					// Check for rowspan and colspan attributes
					val rowSpan = getRowSpan(cellNode)
					val colSpan = getColSpan(cellNode)

					cells.add(cellContent)

					if (rowSpan > 1) {
						rowSpans[columnIndex] = CellSpan(cellContent, rowSpan - 1)
					}

					columnIndex++

					// Handle colspan by adding empty cells for the additional columns
					// Note: Markdown doesn't support colspan, so we just skip those columns
					for (i in 1 until colSpan) {
						cells.add(cellContent) // Repeat content for colspan
						if (rowSpan > 1) {
							rowSpans[columnIndex] = CellSpan(cellContent, rowSpan - 1)
						}
						columnIndex++
					}
				} else {
					// No more cells from this row and no spanning cell, we're done
					break
				}
			}

			if (cells.isNotEmpty()) {
				if (isHeaderRow) headerRowIndices.add(rows.size)
				rows.add(cells)
			}
		}
	}

	private fun getColSpan(cell: WtNode): Int {
		// Check for colspan attribute in the cell's XML attributes
		var colSpan = 1
		when (cell) {
			is WtTableHeader -> {
				cell.xmlAttributes?.let { attrs ->
					iterate(attrs) { attr ->
						if (attr is WtXmlAttribute && attr.name.asString.equals("colspan", ignoreCase = true)) {
							val value = attr.value
							if (value is WtValue) {
								val text = extractCellContent(value)
								colSpan = text.toIntOrNull() ?: 1
							}
						}
					}
				}
			}
			is WtTableCell -> {
				cell.xmlAttributes?.let { attrs ->
					iterate(attrs) { attr ->
						if (attr is WtXmlAttribute && attr.name.asString.equals("colspan", ignoreCase = true)) {
							val value = attr.value
							if (value is WtValue) {
								val text = extractCellContent(value)
								colSpan = text.toIntOrNull() ?: 1
							}
						}
					}
				}
			}
		}
		return colSpan
	}

	private fun getRowSpan(cell: WtNode): Int {
		// Check for rowspan attribute in the cell's XML attributes
		var rowSpan = 1
		when (cell) {
			is WtTableHeader -> {
				cell.xmlAttributes?.let { attrs ->
					iterate(attrs) { attr ->
						if (attr is WtXmlAttribute && attr.name.asString.equals("rowspan", ignoreCase = true)) {
							val value = attr.value
							if (value is WtValue) {
								val text = extractCellContent(value)
								rowSpan = text.toIntOrNull() ?: 1
							}
						}
					}
				}
			}
			is WtTableCell -> {
				cell.xmlAttributes?.let { attrs ->
					iterate(attrs) { attr ->
						if (attr is WtXmlAttribute && attr.name.asString.equals("rowspan", ignoreCase = true)) {
							val value = attr.value
							if (value is WtValue) {
								val text = extractCellContent(value)
								rowSpan = text.toIntOrNull() ?: 1
							}
						}
					}
				}
			}
		}
		return rowSpan
	}

	private fun extractCellContent(node: WtNode?): String {
		if (node == null) return ""
		val sb = StringBuilder()
		collectCellContent(node, sb)
		var text = sb.toString().trim()
		// Collapse multiple whitespace
		text = text.replace(Regex("\\s+"), " ")
		// Remove space before punctuation
		text = text.replace(Regex(" (?=[,.!?;:])"), "")
		// Normalize comma spacing
		text = text.replace(Regex(",\\s*"), ", ")
		// Remove trailing commas/spaces
		text = text.replace(Regex(",\\s*$"), "")
		return text
	}

	private fun collectCellContent(node: WtNode, sb: StringBuilder) {
		when (node) {
			is WtText -> {
				val raw = node.content
				if (raw.isNotBlank()) {
					val piece = raw.trim()
					appendWithSpaceIfNeeded(sb, piece)
				}
			}
			is WtInternalLink -> {
				val hasContentBefore = sb.isNotEmpty() && !sb.endsWith(" ") && !sb.endsWith(", ")
				val title = if (node.hasTitle()) node.title else null
				val beforeLen = sb.length
				if (title != null && !title.isEmpty()) {
					collectCellContent(title, sb)
				} else if (node.target is WtPageName) {
					collectCellContent(node.target, sb)
				}
				// Ensure space inserted if content appended directly to previous word
				if (hasContentBefore && beforeLen != sb.length && !sb.endsWith(" ")) sb.insert(beforeLen, " ")
			}
			is WtTemplate -> {
				templateVisitor.parseToStringOrNull(node)?.let {
					appendWithSpaceIfNeeded(sb, it)
				}
			}
			is WtTagExtension -> {}
			is WtXmlElement -> {
				val name = node.name.lowercase()
				if (name == "br") {
					// Replace break with comma separator only if there's already content and not ending with comma
					if (sb.isNotEmpty() && !sb.endsWith(", ")) {
						sb.append(", ")
					}
				} else {
					iterate(node.body) { child -> collectCellContent(child, sb) }
				}
			}
			is WtNodeList, is WtBody, is WtParagraph, is WtValue, is WtPageName ->
				iterate(node) { child -> collectCellContent(child, sb) }
			else -> {
				try {
					iterate(node) { child -> collectCellContent(child, sb) }
				} catch (_: Exception) {}
			}
		}
	}

	private fun appendWithSpaceIfNeeded(sb: StringBuilder, piece: String) {
		if (piece.isEmpty()) return
		if (sb.isNotEmpty()) {
			val last = sb.last()
			if (!last.isWhitespace() && last != ',' && piece.firstOrNull()?.let { !it.isWhitespace() && it != ',' } == true) {
				sb.append(' ')
			}
		}
		sb.append(piece)
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

	private fun buildMarkdownTable(rows: List<List<String>>, columnWidths: List<Int>, headerIndex: Int): String {
		val sb = StringBuilder()
		val headerSeparator = buildSeparator(columnWidths)

		for ((i, row) in rows.withIndex()) {
			sb.append(buildRow(row, columnWidths))
			if (i == headerIndex) {
				sb.append(headerSeparator)
			}
		}
		return sb.toString()
	}

	private fun buildRow(cells: List<String>, columnWidths: List<Int>): String {
		val paddedCells = cells.mapIndexed { i, cell ->
			cell.padEnd(columnWidths.getOrElse(i) { 0 })
		}
		return "| " + paddedCells.joinToString(" | ") + " |\n"
	}

	private fun buildSeparator(columnWidths: List<Int>): String {
		// Add 2 to each width to account for the spaces around content in data rows
		val separators = columnWidths.map { "-".repeat(it + 2) }
		return "|${separators.joinToString("|")}|\n"
	}

	private fun <T> iterate(node: WtNode?, action: (WtNode) -> T) {
		if (node == null) return
		try {
			for (child in node) {
				if (child is WtNode) {
					action(child)
				}
			}
		} catch (e: Exception) {
			// Node might not be iterable
		}
	}
}
