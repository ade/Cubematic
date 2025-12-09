package se.ade.mc.cubematic.agent.rags

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import se.ade.mc.cubematic.agent.main.ProcessEvent

@LLMDescription("Tools for gathering information")
class RagServerTools(private val onProcessEvent: (ProcessEvent) -> Unit) : ToolSet {
	private val ragClient: RagClient = DefaultRagClient()
	@Tool
	@LLMDescription("Searches for query in Minecraft Wiki using text embeddings.")
	suspend fun search(
		@LLMDescription("The search query (without Minecraft or Minecraft Wiki).")
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
	@LLMDescription("Read a specific page from the Minecraft Wiki, to get detailed information.")
	suspend fun readPage(
		@LLMDescription("The title/name of the page")
		pageName: String,
	): String {
		return ragClient.getPage(pageName).getOrThrow()
	}

	@Tool
	@LLMDescription("Send a message to the user about what you are thinking or doing (but not the answer to the question). " +
			"Never provide your answer here, just inform the user about the progress " +
			"when calling tools. For example, you can say 'I'll check the Diamond page for info about where to find them.', " +
			" or 'Reading about Enchanting Table to see how to make one.'")
	suspend fun sendStatusMessage(
		@LLMDescription("The message to send")
		message: String,
	) {
		onProcessEvent(ProcessEvent.ProgressMessage(message))
	}
}