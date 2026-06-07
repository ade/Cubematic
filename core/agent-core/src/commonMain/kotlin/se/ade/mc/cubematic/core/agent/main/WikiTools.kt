package se.ade.mc.cubematic.core.agent.main

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import se.ade.mc.cubematic.core.agent.wiki.CoreWikiClient
import se.ade.mc.cubematic.core.agent.wiki.WikiTextPruner

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
		@LLMDescription("The title/name of the page.")
		title: String,
		@LLMDescription("Few-word status update to user of why the page is being read.")
		rationale: String,
	): String {
		val pageContent = wikiClient.getPageContent(title)
			?.let {
				// Check for redirect
				val redirectTarget = "^#REDIRECT \\[\\[(.*)]]".toRegex(RegexOption.IGNORE_CASE)
					.find(it.trim())
					?.groups
					?.get(1)
					?.value

				if (redirectTarget == null) it
				else {
					// Fetch the redirect target instead
					//println("$title redirecting to $redirectTarget")
					return@let getPageContent(redirectTarget, rationale)
				}
			}
			?: return "Page not found: $title"

		val pruned = WikiTextPruner.prune(pageContent)

		val markdown = WikiTextConvert.optimizeForAgent(title, pruned)

		//println(markdown)

		return markdown
	}
}