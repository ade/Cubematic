package se.ade.mc.wiki.md

/**
 * DSL builder for creating markdown documents with a structured intermediate representation.
 *
 * Example usage:
 * ```
 * val doc = markdown {
 *     heading(1, "My Document")
 *     paragraph {
 *         text("Hello ")
 *         bold("world")
 *         text("!")
 *     }
 *     codeBlock("println(\"Hello\")", "kotlin")
 * }
 * val output = doc.render()
 * ```
 */
class MarkdownDocumentBuilder {
	private val nodes = mutableListOf<MarkdownNode>()

	/**
	 * Add a heading at the specified level
	 */
	fun heading(level: Int, content: String) {
		nodes.add(MarkdownNode.Heading(level, content))
	}

	/**
	 * Add a section with a heading and nested content
	 * Example:
	 * ```
	 * section(1, "Main Section") {
	 *     text("Some content")
	 *     section(2, "Subsection") {
	 *         text("Nested content")
	 *     }
	 * }
	 * ```
	 */
	fun section(level: Int, title: String, builder: MarkdownDocumentBuilder.() -> Unit) {
		val sectionBuilder = MarkdownDocumentBuilder()
		sectionBuilder.builder()
		nodes.add(MarkdownNode.Section(level, title, sectionBuilder.build().nodes))
	}

	/**
	 * Add plain text
	 */
	fun text(content: String) {
		nodes.add(MarkdownNode.Text(content))
	}

	/**
	 * Add bold text
	 */
	fun bold(content: String) {
		nodes.add(MarkdownNode.Bold(content))
	}

	/**
	 * Add italic text
	 */
	fun italic(content: String) {
		nodes.add(MarkdownNode.Italic(content))
	}

	/**
	 * Add a code block with optional language
	 */
	fun codeBlock(code: String, language: String = "") {
		nodes.add(MarkdownNode.CodeBlock(code, language))
	}

	/**
	 * Add a raw/preformatted block
	 */
	fun rawBlock(content: String) {
		nodes.add(MarkdownNode.RawBlock(content))
	}

	/**
	 * Add a table
	 */
	fun table(rows: List<List<String>>, headerRowIndex: Int = 0) {
		nodes.add(MarkdownNode.Table(rows, headerRowIndex))
	}

	/**
	 * Add a horizontal rule
	 */
	fun horizontalRule() {
		nodes.add(MarkdownNode.HorizontalRule)
	}

	/**
	 * Add a line break
	 */
	fun lineBreak() {
		nodes.add(MarkdownNode.LineBreak)
	}

	/**
	 * Request blank lines for spacing
	 */
	fun blankLines(count: Int = 1) {
		nodes.add(MarkdownNode.BlankLines(count))
	}

	/**
	 * Add a space
	 */
	fun space() {
		nodes.add(MarkdownNode.Space)
	}

	/**
	 * Add a redacted template marker
	 */
	fun templateRedacted(templateName: String) {
		nodes.add(MarkdownNode.TemplateRedacted(templateName))
	}

	/**
	 * Add a paragraph with nested content
	 */
	fun paragraph(builder: MarkdownDocumentBuilder.() -> Unit) {
		val paragraphBuilder = MarkdownDocumentBuilder()
		paragraphBuilder.builder()
		nodes.add(MarkdownNode.Paragraph(paragraphBuilder.build().nodes))
	}

	/**
	 * Add a generic block with nested content
	 */
	fun block(builder: MarkdownDocumentBuilder.() -> Unit) {
		val blockBuilder = MarkdownDocumentBuilder()
		blockBuilder.builder()
		nodes.add(MarkdownNode.Block(blockBuilder.build().nodes))
	}

	/**
	 * Add an unordered list with items
	 */
	fun unorderedList(builder: ListBuilder.() -> Unit) {
		val listBuilder = ListBuilder()
		listBuilder.builder()
		nodes.add(MarkdownNode.UnorderedList(listBuilder.build()))
	}

	/**
	 * Add an ordered list with items
	 */
	fun orderedList(builder: ListBuilder.() -> Unit) {
		val listBuilder = ListBuilder()
		listBuilder.builder()
		nodes.add(MarkdownNode.OrderedList(listBuilder.build()))
	}

	/**
	 * Builder for list items
	 */
	class ListBuilder {
		private val items = mutableListOf<MarkdownNode.ListItem>()

		/**
		 * Add a list item with content
		 */
		fun item(builder: MarkdownDocumentBuilder.() -> Unit) {
			val itemBuilder = MarkdownDocumentBuilder()
			itemBuilder.builder()
			items.add(MarkdownNode.ListItem(itemBuilder.build().nodes))
		}

		fun build() = items.toList()
	}

	/**
	 * Add a node directly (for custom nodes or when you already have a node)
	 */
	fun node(node: MarkdownNode) {
		nodes.add(node)
	}

	/**
	 * Add multiple nodes
	 */
	fun nodes(vararg nodes: MarkdownNode) {
		this.nodes.addAll(nodes)
	}

	/**
	 * Build the list of nodes
	 */
	fun build() = MarkdownDocument(nodes.toList())

	/**
	 * Build and render to a string
	 */
	fun render(wrapCol: Int = 0): String {
		val renderer = MarkdownRenderer(wrapCol)
		return renderer.render(nodes)
	}
}