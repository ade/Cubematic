package se.ade.mc.cubematic.core.agent.main

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.context.AIAgentFunctionalContext
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.feature.handler.agent.AgentCompletedContext
import ai.koog.agents.core.feature.handler.agent.AgentStartingContext
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.Prompt
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.prompt.streaming.toMessageResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import se.ade.mc.cubematic.core.agent.config.InferenceProvider
import se.ade.mc.cubematic.core.agent.rags.QueryResponse

class MainAgent(
	val history: List<Message>,
	val context: QueryContext,
	val ragData: QueryResponse?,
	provider: InferenceProvider,
	val onProcessEvent: (ProcessEvent) -> Unit
) {
	private var processCounter = 0

	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	private val agentConfig = AIAgentConfig(
		prompt = Prompt.build("agent") {
			system {
				text("""
                You are QB, an advanced minecraft assistant, for use inside the game chat.
				You answer questions by searching the Minecraft wiki using the provided tools.
				You are a stoic and precise assistant, that only answers based on facts you actively read.
				
				You have no inherent knowledge about Minecraft.
				Pretend you don't know anything other than what you read.
				NEVER answer any question without first knowing up to date facts from the wiki sources,
				such as when searching for the answer using the tools, or from the provided context.
				
				You may however assume that the player DOES know a lot about minecraft and gaming in general.
				That means you can describe things to the player using minecraft terminology.
				(The player may ask follow up questions if they need more details.)
                
				Always respond with a clear, factual message explaining your result and how you motivate it.
				Never make up facts, always use the tools to find the correct information.
				Always use the correct format for calling tools. Specifically, don't markup the json with "```json"
				
				Crafting recipes in the wiki text are described in a grid, corresponding to the crafting table layout.
				Each row in the grid represents a row in the crafting table, and each cell contains the item name.
				Empty cells are represented by an underscore (_).
				There is a comma between each cell in a row, and a new line between each row.
				Cells with content are described using ONE letter. The mapping from letters to item names is given.
				For example, a recipe for a wooden pickaxe would be described as:
				Grid: (A: Oak Planks, B: Stick):
				A,A,A
				_,B,_
				_,B,_
				
				Answers should fit in the game chat.
				That means your answers should be short and to the point, as if you were a player answering in chat.
				Only answer the direct question, don't add any extra information.
				
				Formatting:
				ONLY use plain text. Newlines are permitted.
				The text will be rendered to the player as-is without any formatting support.
                
				Respond in a gaming oriented style, as if you were a friendly and helpful player in the game chat.
				Always use informal abbreviations to make words shorter where possible (e.g., "info" instead of "information")
				There is no need to use full sentences, keep it short and to the point.
				Respond only to the last question asked, ignore previous questions and answers in the chat history.
				It is imperative that you research all aspects of the question using the tools before answering.
				If you are researching a complex question, provide progress update messages using the relevant tool call.
				
				You are running inside a server side Minecraft mod that allows players to chat with you.
				
				Information about the player who is chatting with you follows:
				$context
				
                """.trimIndent())

				if(ragData != null) {
					text("The following information was retrieved from the Minecraft Wiki using a RAG system.\n")
					text("You may use facts from this information when answering the user's question.\n")
					text("If the information is not sufficient to answer, use the provided tools to acquire more data.\n")
					text("Note that the information may be pruned and not complete, read the full page content when needed.\n")
					text("RAG Information:\n")
					text(ragData.chunks.joinToString("\n\n---\n\n") {
						"""
					Page: ${it.pageName}
					Content: ${it.content}
					Breadcrumbs: ${it.breadcrumbs.joinToString(" > ")}
					
					Text: ${it.content}
					""".trimIndent()
					})
				}

				if(context.chatHistory.isNotEmpty()) {
					text("""
					For context, here is the public chat history.
					The format mirrors that of the minecraft chat e.g. <name> <message> (oldest messages first).
					Note that all or parts of this history may be irrelevant to the question asked, ignore if so.
					History:
					""".trimIndent())

					for (msg in context.chatHistory) {
						text("<${msg.first}> ${msg.second}\n\n")
					}
					text("(End of chat history.)")
				}
			}
			history.forEach {
				message(it)
			}
		},
		model = provider.model,
		maxAgentIterations = 50,
	)

	// Add the tool to the tool registry
	private val toolRegistry = ToolRegistry {
		tools(WikiTools())
		//tools(RagServerTools(onProcessEvent))
	}

	//val logger = KotlinLogging.logger { }

	val agent = AIAgent<String, String>(
		promptExecutor = provider.executor,
		toolRegistry = toolRegistry,
		strategy = functionalStrategy { input ->
			runQuery(input)
		},
		agentConfig = agentConfig
	) {
		install(EventHandler.Feature) {
			onAgentStarting { eventContext: AgentStartingContext ->
				//println("Starting agent: ${eventContext.agent.id}")
			}
			onAgentCompleted { eventContext: AgentCompletedContext ->
				//println("Result:\n${eventContext.result}")
			}
		}
	}

	private suspend fun AIAgentFunctionalContext.runQuery(input: String): String {
		val rootId = processCounter++
		onProcessEvent(ProcessEvent.Update(ProcessEntry(rootId, "Parsing/Reasoning...")))
		val r = requestLLMStreaming(input)
		val frames = mutableListOf<StreamFrame>()

		r.collect {
			frames.add(it)

			when (it) {
				is StreamFrame.DeltaFrame -> {
					when (it) {
						is StreamFrame.TextDelta -> {
							onProcessEvent(ProcessEvent.TextSink(it.text))
						}
						is StreamFrame.ToolCallDelta -> {}
						is StreamFrame.ReasoningDelta -> {}
					}
				}
				is StreamFrame.CompleteFrame -> {
					when (it) {
						is StreamFrame.ToolCallComplete -> {}
						is StreamFrame.TextComplete -> {}
						is StreamFrame.ReasoningComplete -> {}
					}
				}
				is StreamFrame.End -> {}
			}
		}

		onProcessEvent(ProcessEvent.Update(ProcessEntry(rootId, "Parsing/Reasoning", done = true)))

		val reply = frames.toMessageResponse().textContent().takeIf { it.isNotEmpty() }
		if(reply != null) {
			return reply
		}

		val toolCalls = frames.toMessageResponse().parts.filterIsInstance<MessagePart.Tool.Call>()
		assert(toolCalls.size == 1) {
			"Expected one tool call, got ${toolCalls.size}"
		}

		val currentToolCall = toolCalls[0]

		val response = resolveCall(currentToolCall)

		return response
	}

	private suspend fun AIAgentFunctionalContext.resolveCall(tc: MessagePart.Tool.Call): String {
		println("Resolving tool call: ${tc.tool} (${tc.args})")

		val id = processCounter++
		onProcessEvent(ProcessEvent.Update(ProcessEntry(id, "Call tool ${tc.tool}: ${tc.args}")))

		llm.writeSession {
			appendPrompt {
				toolCall(tc)
			}
		}

		val toolResult = executeTool(tc)

		val out = llm.writeSession {
			appendPrompt {
				toolResult(toolResult.toMessagePart())
			}
			val stream = requestLLMStreaming()
			val f2 = mutableListOf<StreamFrame>()
			stream.collect {
				val addText = when(it) {
					is StreamFrame.TextDelta -> it.text
					is StreamFrame.TextComplete -> it.text
					else -> null
				}
				if(addText != null) {
					//sink(it.text)
					onProcessEvent(ProcessEvent.TextSink(addText))
				}
				f2.add(it)
			}
			f2
		}

		onProcessEvent(ProcessEvent.Update(ProcessEntry(id, "Call tool ${tc.tool}: ${tc.args}", done = true)))

		val tools = out.toMessageResponse().parts.filterIsInstance<MessagePart.Tool.Call>()
		return if(tools.isNotEmpty()) {
			assert(tools.size == 1) {
				"Expected at most one tool call, got ${tools.size}"
			}
			resolveCall(tools[0])
		} else {
			out.toMessageResponse().textContent()
				?: throw IllegalStateException("Expected an assistant message or a tool call, got nothing")
		}
	}
}

/*
public fun Iterable<StreamFrame>.toToolCallMessages(): List<MessagePart.Tool.Call> =
	toToolCallMessages().filterIsInstance<Message.Tool.Call>()
*/
/*
public fun Iterable<StreamFrame>.toAssistantMessageOrNull(): Message.Assistant? =
	toMessageResponse().filterIsInstance<Message.Assistant>().singleOrNull()
*/
