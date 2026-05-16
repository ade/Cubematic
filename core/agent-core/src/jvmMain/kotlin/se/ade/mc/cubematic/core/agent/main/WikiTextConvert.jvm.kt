package se.ade.mc.cubematic.core.agent.main

import se.ade.mc.wiki.md.main.nodes.wikiTextToMarkdown

actual object WikiTextConvert {
	actual fun optimizeForAgent(pageTitle: String, pageContent: String): String {
		return wikiTextToMarkdown(pageTitle, pageContent).render()
	}
}