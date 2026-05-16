package se.ade.mc.cubematic.core.agent.main

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import se.ade.mc.cubematic.core.agent.wiki.CoreWikiClient
import se.ade.mc.cubematic.core.agent.wiki.WikiParser

// Implement a simple calculator tool that can add two numbers
@LLMDescription("Tools for gathering information")
class WikiTools : ToolSet {
	private val wikiClient = CoreWikiClient()

	@Tool
	@LLMDescription("Search for query in Minecraft Wiki and return a list of comma separated page names/titles.")
	suspend fun search(
		@LLMDescription("The search query. Should not be a sentence, but a keyword or a few keywords at most.")
		query: String,
	): String {
		return wikiClient.searchPages(query).joinToString(", ")
	}

	@Tool
	@LLMDescription("Get the full content of a page from Minecraft Wiki, converted to markdown.")
	suspend fun getPageContent(
		@LLMDescription("The title/name of the page")
		title: String,
	): String {
		val pageContent = wikiClient.getPageContent(title)
			?: return "No content found for page: $title"

		val pruned = WikiParser().pruneSections(pageContent)

		val markdown = WikiTextConvert.optimizeForAgent(title, pruned)

		println(markdown)

		return markdown
	}
}