package se.ade.mc.wiki.md.main

import de.fau.cs.osr.ptk.common.AstVisitor
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.nodes.EngPage
import org.sweble.wikitext.parser.nodes.WtBold
import org.sweble.wikitext.parser.nodes.WtExternalLink
import org.sweble.wikitext.parser.nodes.WtHorizontalRule
import org.sweble.wikitext.parser.nodes.WtIllegalCodePoint
import org.sweble.wikitext.parser.nodes.WtImageLink
import org.sweble.wikitext.parser.nodes.WtInternalLink
import org.sweble.wikitext.parser.nodes.WtItalics
import org.sweble.wikitext.parser.nodes.WtListItem
import org.sweble.wikitext.parser.nodes.WtNode
import org.sweble.wikitext.parser.nodes.WtNodeList
import org.sweble.wikitext.parser.nodes.WtOrderedList
import org.sweble.wikitext.parser.nodes.WtPageSwitch
import org.sweble.wikitext.parser.nodes.WtParagraph
import org.sweble.wikitext.parser.nodes.WtSection
import org.sweble.wikitext.parser.nodes.WtTable
import org.sweble.wikitext.parser.nodes.WtTagExtension
import org.sweble.wikitext.parser.nodes.WtTemplate
import org.sweble.wikitext.parser.nodes.WtTemplateArgument
import org.sweble.wikitext.parser.nodes.WtTemplateParameter
import org.sweble.wikitext.parser.nodes.WtText
import org.sweble.wikitext.parser.nodes.WtUnorderedList
import org.sweble.wikitext.parser.nodes.WtUrl
import org.sweble.wikitext.parser.nodes.WtWhitespace
import org.sweble.wikitext.parser.nodes.WtXmlCharRef
import org.sweble.wikitext.parser.nodes.WtXmlComment
import org.sweble.wikitext.parser.nodes.WtXmlElement
import org.sweble.wikitext.parser.nodes.WtXmlEntityRef

abstract class BaseVisitor: AstVisitor<WtNode>() {
	override fun before(node: WtNode?): WtNode? {
		return super.before(node)
	}

	abstract override fun after(node: WtNode?, result: Any?): Any

	open fun visit(n: WtNode) {}
	open fun visit(n: WtTable) {}
	open fun visit(n: WtNodeList?) {}
	open fun visit(e: WtUnorderedList?) {}
	open fun visit(e: WtOrderedList?) {}
	open fun visit(item: WtListItem?) {}
	open fun visit(p: EngPage?) {}
	open fun visit(text: WtText) {}
	open fun visit(w: WtWhitespace?) {}
	open fun visit(b: WtBold?) {}
	open fun visit(i: WtItalics?) {}
	open fun visit(cr: WtXmlCharRef) {}
	open fun visit(er: WtXmlEntityRef) {}
	open fun visit(wtUrl: WtUrl) {}
	open fun visit(link: WtExternalLink?) {}
	open fun visit(link: WtInternalLink) {}
	open fun visit(s: WtSection) {}
	open fun visit(p: WtParagraph?) {}
	open fun visit(hr: WtHorizontalRule?) {}
	open fun visit(e: WtXmlElement) {}
	open fun visit(n: WtTemplate) {}
	open fun visit(n: WtImageLink?) {}
	open fun visit(n: WtIllegalCodePoint?) {}
	open fun visit(n: WtXmlComment?) {}
	open fun visit(n: WtTemplateArgument?) {}
	open fun visit(n: WtTemplateParameter?) {}
	open fun visit(n: WtTagExtension?) {}
	open fun visit(n: WtPageSwitch?) {}
}