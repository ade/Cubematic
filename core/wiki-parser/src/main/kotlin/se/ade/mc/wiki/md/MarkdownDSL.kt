package se.ade.mc.wiki.md

/**
 * Top-level DSL function to create a markdown document
 */
fun markdown(wrapCol: Int = 80, builder: MarkdownDocumentBuilder.() -> Unit): MarkdownDocument {
	val docBuilder = MarkdownDocumentBuilder()
	docBuilder.builder()
	return MarkdownDocument(docBuilder.build().nodes)
}

