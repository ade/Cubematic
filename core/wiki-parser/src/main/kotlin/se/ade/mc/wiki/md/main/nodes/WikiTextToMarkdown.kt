package se.ade.mc.wiki.md.main.nodes

import org.sweble.wikitext.engine.PageId
import org.sweble.wikitext.engine.PageTitle
import org.sweble.wikitext.engine.WtEngineImpl
import org.sweble.wikitext.engine.config.WikiConfig
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp
import se.ade.mc.wiki.md.MarkdownDocument
import se.ade.mc.wiki.md.MarkdownNormalizer
import se.ade.mc.wiki.md.WikiToMdVisitor

fun wikiTextToMarkdown(fileTitle: String, wikiText: String): MarkdownDocument {
	// Set-up a simple wiki configuration
	val config: WikiConfig = DefaultConfigEnWp.generate()

	// Instantiate a compiler for wiki pages
	val engine = WtEngineImpl(config)

	// Retrieve a page
	val pageTitle = PageTitle.make(config, fileTitle)

	val pageId = PageId(pageTitle, -1)

	// Compile the retrieved page
	val cp = engine.postprocess(pageId, wikiText, null)

	val p = WikiToMdVisitor(fileTitle, config)
	val document = p.go(cp.getPage()) as MarkdownDocument

	// Normalize the document to group consecutive inline nodes into paragraphs
	return MarkdownNormalizer.normalize(document)
}