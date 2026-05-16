package se.ade.mc.wiki.md

/**
 * Sealed class hierarchy representing the intermediate markdown document structure.
 * This allows us to build a structured representation before converting to text.
 */
sealed class MarkdownNode {
	/**
	 * Interface for nodes that contain child nodes
	 */
	interface HasChildren {
		val children: List<MarkdownNode>
		fun withChildren(children: List<MarkdownNode>): MarkdownNode
	}

	/**
	 * A text node containing inline content
	 */
	data class Text(val content: String) : MarkdownNode()

	/**
	 * A section with a heading and content (including nested sections)
	 */
	data class Section(
		val level: Int,
		val title: String,
		override val children: List<MarkdownNode> = emptyList()
	) : MarkdownNode(), HasChildren {
		override fun withChildren(children: List<MarkdownNode>) = copy(children = children)
	}

	/**
	 * A heading with level (1-6) and content (for standalone headings without body)
	 */
	data class Heading(val level: Int, val content: String) : MarkdownNode()

	/**
	 * A paragraph containing inline nodes
	 */
	data class Paragraph(
		override val children: List<MarkdownNode>
	) : MarkdownNode(), HasChildren {
		override fun withChildren(children: List<MarkdownNode>) = copy(children = children)
	}

	/**
	 * A block containing multiple nodes (generic container)
	 */
	data class Block(
		override val children: List<MarkdownNode>
	) : MarkdownNode(), HasChildren {
		override fun withChildren(children: List<MarkdownNode>) = copy(children = children)
	}

	/**
	 * Bold text
	 */
	data class Bold(val content: String) : MarkdownNode()

	/**
	 * Italic text
	 */
	data class Italic(val content: String) : MarkdownNode()

	/**
	 * A code block with optional language
	 */
	data class CodeBlock(val code: String, val language: String = "") : MarkdownNode()

	/**
	 * A raw/preformatted text block
	 */
	data class RawBlock(val content: String) : MarkdownNode()

	/**
	 * A markdown table
	 */
	data class Table(val rows: List<List<String>>, val headerRowIndex: Int = 0) : MarkdownNode()

	/**
	 * An unordered list (bullets)
	 */
	data class UnorderedList(val items: List<ListItem>) : MarkdownNode()

	/**
	 * An ordered list (numbered)
	 */
	data class OrderedList(val items: List<ListItem>) : MarkdownNode()

	/**
	 * A list item that can contain inline content and nested lists
	 */
	data class ListItem(
		override val children: List<MarkdownNode>
	) : MarkdownNode(), HasChildren {
		override fun withChildren(children: List<MarkdownNode>) = copy(children = children)
	}

	/**
	 * A horizontal rule/separator
	 */
	data object HorizontalRule : MarkdownNode()

	/**
	 * A line break
	 */
	data object LineBreak : MarkdownNode()

	/**
	 * Request for blank lines (spacing directive)
	 */
	data class BlankLines(val count: Int) : MarkdownNode()

	/**
	 * Space character
	 */
	data object Space : MarkdownNode()

	/**
	 * A redacted/unrecognized template with the template name preserved
	 */
	data class TemplateRedacted(val templateName: String) : MarkdownNode()

	/**
	 * Render this node and its children to a markdown string
	 */
	fun render(wrapCol: Int = 0): String {
		val renderer = MarkdownRenderer(wrapCol)
		return renderer.render(this)
	}
}
