package se.ade.mc.wiki.md

/**
 * Represents a complete markdown document
 */
data class MarkdownDocument(
	val nodes: List<MarkdownNode>
) {
	/**
	 * Render the document to a string
	 */
	fun render(wrapCol: Int = 0): String {
		val renderer = MarkdownRenderer(wrapCol)
		return renderer.render(nodes)
	}

	/**
	 * Combine with another document
	 */
	operator fun plus(other: MarkdownDocument): MarkdownDocument {
		return MarkdownDocument(nodes + other.nodes)
	}

	/**
	 * Remove TemplateRedacted nodes and empty sections from the document.
	 * A section is considered empty if it has no children after pruning.
	 * @param dropTables if true, replace tables with "(Table redacted)" text nodes
	 */
	fun prune(dropTables: Boolean = true): MarkdownDocument {
		return MarkdownDocument(pruneNodes(nodes, dropTables))
	}

	private fun pruneNodes(nodes: List<MarkdownNode>, dropTables: Boolean): List<MarkdownNode> {
		return nodes.mapNotNull { node ->
			when (node) {
				// Drop TemplateRedacted nodes
				is MarkdownNode.TemplateRedacted -> null

				// Replace tables with redacted text if dropTables is enabled
				is MarkdownNode.Table -> {
					if (dropTables) {
						MarkdownNode.Paragraph(
							listOf(MarkdownNode.Text("(Table redacted, see fulltext)"))
						)
					} else {
						node
					}
				}

				// Handle all nodes with children using the HasChildren interface
				is MarkdownNode.HasChildren -> {
					val prunedChildren = pruneNodes(node.children, dropTables)
					// Check if the node has any meaningful content after pruning
					if (prunedChildren.isEmpty() || !hasContent(prunedChildren)) {
						null
					} else {
						node.withChildren(prunedChildren)
					}
				}

				// Handle lists specially (they have items, not children)
				is MarkdownNode.UnorderedList -> {
					val prunedItems = node.items.mapNotNull { item ->
						val prunedChildren = pruneNodes(item.children, dropTables)
						if (prunedChildren.isEmpty() || !hasContent(prunedChildren)) null else item.copy(children = prunedChildren)
					}
					if (prunedItems.isEmpty()) null else node.copy(items = prunedItems)
				}

				is MarkdownNode.OrderedList -> {
					val prunedItems = node.items.mapNotNull { item ->
						val prunedChildren = pruneNodes(item.children, dropTables)
						if (prunedChildren.isEmpty() || !hasContent(prunedChildren)) null else item.copy(children = prunedChildren)
					}
					if (prunedItems.isEmpty()) null else node.copy(items = prunedItems)
				}

				// Keep all other nodes as-is
				else -> node
			}
		}
	}

	/**
	 * Check if a list of nodes contains any meaningful content (not just whitespace).
	 * Returns false for nodes that are empty, contain only whitespace, or only spacing directives.
	 */
	private fun hasContent(nodes: List<MarkdownNode>): Boolean {
		return nodes.any { node ->
			when (node) {
				// These nodes are considered "no content" even if present
				is MarkdownNode.TemplateRedacted -> false
				is MarkdownNode.Space -> false
				is MarkdownNode.LineBreak -> false
				is MarkdownNode.BlankLines -> false

				// Text nodes only have content if non-blank
				is MarkdownNode.Text -> node.content.isNotBlank()

				// Container nodes have content if their children do
				is MarkdownNode.HasChildren -> hasContent(node.children)

				// Lists have content if their items do
				is MarkdownNode.UnorderedList -> node.items.any { hasContent(it.children) }
				is MarkdownNode.OrderedList -> node.items.any { hasContent(it.children) }

				// All other nodes (headings, tables, code blocks, etc.) are considered content
				else -> true
			}
		}
	}
}