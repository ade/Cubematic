package se.ade.mc.wiki.md

/**
 * Normalizes a markdown document by grouping consecutive inline nodes into paragraphs
 */
object MarkdownNormalizer {

	/**
	 * Normalize a document by wrapping consecutive inline nodes in Paragraph containers
	 */
	fun normalize(document: MarkdownDocument): MarkdownDocument {
		return MarkdownDocument(normalizeNodes(document.nodes))
	}

	/**
	 * Normalize a list of nodes by grouping consecutive inline nodes
	 */
	private fun normalizeNodes(nodes: List<MarkdownNode>): List<MarkdownNode> {
		val result = mutableListOf<MarkdownNode>()
		val inlineBuffer = mutableListOf<MarkdownNode>()

		fun flushInlineBuffer() {
			if (inlineBuffer.isNotEmpty()) {
				// Wrap accumulated inline nodes in a Paragraph
				result.add(MarkdownNode.Paragraph(inlineBuffer.toList()))
				inlineBuffer.clear()
			}
		}

		for (node in nodes) {
			when {
				isInlineNode(node) -> {
					// Accumulate inline nodes
					inlineBuffer.add(node)
				}
				node is MarkdownNode.Section -> {
					// Flush inline buffer before adding section
					flushInlineBuffer()
					// Recursively normalize section children
					val normalizedChildren = normalizeNodes(node.children)
					result.add(MarkdownNode.Section(node.level, node.title, normalizedChildren))
				}
				node is MarkdownNode.Block -> {
					// Flush inline buffer before adding block
					flushInlineBuffer()
					// Recursively normalize block children
					val normalizedChildren = normalizeNodes(node.children)
					result.add(MarkdownNode.Block(normalizedChildren))
				}
				else -> {
					// For other block-level nodes (Paragraph, Table, CodeBlock, etc.), flush and add
					flushInlineBuffer()
					result.add(node)
				}
			}
		}

		// Don't forget to flush any remaining inline nodes
		flushInlineBuffer()

		return result
	}

	/**
	 * Check if a node is an inline element (vs block-level)
	 */
	private fun isInlineNode(node: MarkdownNode): Boolean {
		return when (node) {
			is MarkdownNode.Text,
			is MarkdownNode.Bold,
			is MarkdownNode.Italic,
			is MarkdownNode.Space,
			is MarkdownNode.LineBreak,
			is MarkdownNode.BlankLines -> true
			else -> false
		}
	}
}

