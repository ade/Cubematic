package se.ade.mc.cubematic.agent.rags

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

@LLMDescription("Tools for gathering information")
class RagServerTools: ToolSet {
	private val ragClient: RagClient = DefaultRagClient()
	@Tool
	@LLMDescription("Search for query in Minecraft Wiki using text embeddings.")
	suspend fun search(
		@LLMDescription("The search query")
		query: String,
	): String {
		return ragClient.ragQuery(query, 10).getOrThrow().chunks.joinToString("\n\n---\n\n") {
			"""
			Page: ${it.pageName}
			Content: ${it.content}
			Breadcrumbs: ${it.breadcrumbs.joinToString(" > ")}
			
			Text: ${it.content}
			""".trimIndent()
		}
	}

	@Tool
	@LLMDescription("Get the full page content from the Minecraft Wiki")
	suspend fun getPageContent(
		@LLMDescription("The title/name of the page")
		pageName: String,
	): String {
		return ragClient.getPage(pageName).getOrThrow()
	}
}