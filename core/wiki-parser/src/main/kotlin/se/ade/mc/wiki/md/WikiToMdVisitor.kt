package se.ade.mc.wiki.md

import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.config.WikiConfig
import org.sweble.wikitext.engine.nodes.EngPage
import org.sweble.wikitext.parser.nodes.*
import org.sweble.wikitext.parser.parser.LinkTargetException
import se.ade.mc.wiki.md.main.BaseVisitor
import se.ade.mc.wiki.md.main.nodes.TableVisitor
import se.ade.mc.wiki.md.main.nodes.TemplateVisitor

/**
 * A visitor to convert an article AST into a MarkdownDocument representation. To
 * better understand the visitor pattern as implemented by the Visitor class,
 * please take a look at the following resources:
 *
 *  * [http://en.wikipedia.org/wiki/Visitor_pattern](http://en.wikipedia.org/wiki/Visitor_pattern) (classic pattern)
 *  * [http://www.javaworld.com/javaworld/javatips/jw-javatip98.html](http://www.javaworld.com/javaworld/javatips/jw-javatip98.html) (the version we use here)
 *
 *
 * The methods needed to descend into an AST and visit the children of a given node `n` are
 *  * `dispatch(n)` - visit node `n`,
 *  * `iterate(n)` - visit the **children** of node `n`,
 *  * `map(n)` - visit the **children** of node `n` and gather the return values of the `visit()` calls in a list,
 *  * `mapInPlace(n)` - visit the **children** of node `n` and replace each child node `c` with the return
 *      value of the call to `visit(c)`.
 *
 */
class WikiToMdVisitor(
	private val pageName: String,
	private val config: WikiConfig
) : BaseVisitor() {
	private var builder = MarkdownDocumentBuilder()
	private var extLinkNum = 1
	private val templateVisitor = TemplateVisitor(this, pageName)
	private val tableVisitor = TableVisitor(templateVisitor)

	override fun after(node: WtNode?, result: Any?): Any {
		// Wrap the entire document in a top-level section with the page name.
		return MarkdownDocumentBuilder().also {
			it.section(0, pageName) {
				nodes(*builder.build().nodes.toTypedArray())
			}
		}.build()
	}

	// =========================================================================
	/** Fallback for all nodes that are not explicitly handled below */
	override fun visit(n: WtNode) {
		builder.text("<${n.getNodeName()} />")
	}

	override fun visit(n: WtTable) {
		tableVisitor.visit(n, builder)
	}

	override fun visit(n: WtNodeList?) {
		iterate(n)
	}

	override fun visit(e: WtUnorderedList?) {
		if (e == null) return

		// Collect all list items
		val items = mutableListOf<MarkdownNode.ListItem>()

		for (child in e) {
			if (child is WtListItem) {
				// Create a builder for this item's content
				val itemBuilder = MarkdownDocumentBuilder()
				val savedBuilder = builder
				builder = itemBuilder

				// Process the item's children
				iterate(child)

				// Restore the original builder
				builder = savedBuilder

				// Add the collected item
				items.add(MarkdownNode.ListItem(itemBuilder.build().nodes))
			}
		}

		// Add the complete unordered list to the document
		builder.node(MarkdownNode.UnorderedList(items))
	}

	override fun visit(e: WtOrderedList?) {
		if (e == null) return

		// Collect all list items
		val items = mutableListOf<MarkdownNode.ListItem>()

		for (child in e) {
			if (child is WtListItem) {
				// Create a builder for this item's content
				val itemBuilder = MarkdownDocumentBuilder()
				val savedBuilder = builder
				builder = itemBuilder

				// Process the item's children
				iterate(child)

				// Restore the original builder
				builder = savedBuilder

				// Add the collected item
				items.add(MarkdownNode.ListItem(itemBuilder.build().nodes))
			}
		}

		// Add the complete ordered list to the document
		builder.node(MarkdownNode.OrderedList(items))
	}

	override fun visit(item: WtListItem?) {
		// List items are now handled by their parent list visitor
		// This method is called when iterating through the list
		iterate(item)
	}

	override fun visit(p: EngPage?) {
		iterate(p)
	}

	override fun visit(text: WtText) {
		builder.text(text.content)
	}

	override fun visit(@Suppress("UNUSED_PARAMETER") w: WtWhitespace?) {
		builder.space()
	}

	override fun visit(b: WtBold?) {
		builder.text("**")
		iterate(b)
		builder.text("**")
	}

	override fun visit(i: WtItalics?) {
		builder.text("*")
		iterate(i)
		builder.text("*")
	}

	override fun visit(cr: WtXmlCharRef) {
		builder.text(String(Character.toChars(cr.getCodePoint())))
	}


	override fun visit(er: WtXmlEntityRef) {
		val ch = er.getResolved()
		if (ch == null) {
			builder.text("&${er.getName()};")
		} else {
			builder.text(ch)
		}
	}

	override fun visit(wtUrl: WtUrl) {
		if (!wtUrl.getProtocol().isEmpty()) {
			builder.text("${wtUrl.getProtocol()}:")
		}
		builder.text(wtUrl.getPath())
	}

	override fun visit(@Suppress("UNUSED_PARAMETER") link: WtExternalLink?) {
		builder.text("[${extLinkNum++}]")
	}

	override fun visit(link: WtInternalLink) {
		try {
			if (link.getTarget().isResolved()) {
				val page = PageTitle.make(config, link.getTarget().getAsString())
				if (page.getNamespace() == config.getNamespace("Category")) return
			}
		} catch (@Suppress("UNUSED_PARAMETER") e: LinkTargetException) {
		}

		builder.text(link.getPrefix())
		if (!link.hasTitle()) {
			iterate(link.getTarget())
		} else {
			iterate(link.getTitle())
		}
		builder.text(link.getPostfix())
	}

	override fun visit(s: WtSection) {
		// Extract heading title by iterating into it
		val titleBuilder = MarkdownDocumentBuilder()
		val savedBuilder = builder
		builder = titleBuilder
		iterate(s.getHeading())
		val title = titleBuilder.render(0).trim()

		// Create a new builder for the section content
		val sectionContentBuilder = MarkdownDocumentBuilder()
		builder = sectionContentBuilder

		// Process the body (which may contain nested sections)
		iterate(s.getBody())

		// Restore the parent builder
		builder = savedBuilder

		// Add the section with all its collected content
		builder.section(s.getLevel().coerceAtLeast(1), title) {
			// Add all the content nodes that were collected
			sectionContentBuilder.build().nodes.forEach { node(it) }
		}
	}

	override fun visit(p: WtParagraph?) {
		// Wrap paragraph content in a Paragraph node
		builder.paragraph {
			iterate(p)
		}
		builder.blankLines(2)
	}

	override fun visit(@Suppress("UNUSED_PARAMETER") hr: WtHorizontalRule?) {
		builder.horizontalRule()
	}

	override fun visit(e: WtXmlElement) {
		if (e.getName().equals("br", ignoreCase = true)) {
			builder.lineBreak()
		} else {
			iterate(e.getBody())
		}
	}

	/** Custom handling of templates  */
	override fun visit(n: WtTemplate) {
		templateVisitor.parse(n, builder)
	}
}
