package se.ade.mc.cubematic.agent.main

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.AIAgentFunctionalContext
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.executeTool
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.agent.requestLLMStreaming
import ai.koog.agents.core.feature.handler.agent.AgentCompletedContext
import ai.koog.agents.core.feature.handler.agent.AgentStartingContext
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.reflect.tools
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.message.Message
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.prompt.streaming.toAssistantMessageOrNull
import ai.koog.prompt.streaming.toToolCallMessages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import se.ade.mc.cubematic.agent.config.InferenceProvider

class MainAgent(
	val history: List<Message>,
	val context: QueryContext,
	provider: InferenceProvider,
	val sink: (String) -> Unit,
	val onProcessEvent: (ProcessEvent) -> Unit
) {
	private var processCounter = 0

	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
	private val agentConfig = AIAgentConfig(
		prompt = Prompt.Companion.build("simple-calculator") {
			system(
				"""
                You are an advanced minecraft wiki assistant, for use inside the game chat.
				You answer questions by searching the Minecraft wiki using the provided tools.
				You have no inherent knowledge about Minecraft. Pretend you don't know anything other than what you read.
				NEVER answer any question without first searching for the answer using the tools, even if you think you know the answer.
                Always respond with a clear, factual message explaining your result and how you motivate it.
				Never make up facts, always use the tools to find the correct information.
				Always use the correct format for calling tools. Specifically, don't markup the json with "```json"
				Wiki pages are always titled with capital first letter on each word, e.g. "Diamond Sword", "Creeper", "Crafting Table".
				If you receive ONLY a short #REDIRECT notice in a page text from the wiki, you should follow the redirect and get the info from the target page instead.
				
				Understanding the === Crafting === section in the wiki:
				Here we get recipes for crafting, in a crafting table/grid. Columns are lettered A,B,C and rows 1,2,3.
				A is the left column, B the middle column and C the right column.
				1 is the top row, 2 the middle row and 3 the bottom row.
				
				Answers should fit in the game chat. Markdown is not supported in chat so don't use it.
				Line breaks are not supported either, so no paragraphs or line-based formatting.
				That means your answers should be short and to the point, as if you were a player answering in chat.
				Only answer the direct question, don't add any extra information.
                
				Respond in a gaming oriented style, as if you were a friendly and helpful player in the game chat.
				There is no need to use full sentences, keep it short and to the point.
				Respond only to the last question asked, ignore previous questions and answers in the chat history.
				
				You are running inside a server side Minecraft mod that allows players to chat with you.
				
				Information about the player who is chatting with you follows:
				$context
				
                """.trimIndent()
			)
			history.forEach {
				message(it)
			}
		},
		model = provider.model,
		maxAgentIterations = 50,
	)

	// Add the tool to the tool registry
	private val toolRegistry = ToolRegistry.Companion {
		tools(DefaultTools())
	}

	val agent = AIAgent.Companion<String, String>(
		promptExecutor = provider.executor,
		toolRegistry = toolRegistry,
		strategy = functionalStrategy { input ->
			runQuery(input)
		},
		agentConfig = agentConfig,
		installFeatures = {
			install(EventHandler.Feature) {
				onAgentStarting { eventContext: AgentStartingContext<*> ->
					println("Starting agent: ${eventContext.agent.id}")
				}
				onAgentCompleted { eventContext: AgentCompletedContext ->
					println("Result:\n${eventContext.result}")
				}
			}
		}
	)

	private suspend fun AIAgentFunctionalContext.runQuery(input: String): String {
		val rootId = processCounter++
		onProcessEvent(ProcessEvent.Update(ProcessEntry(rootId, "Parsing/Reasoning...")))
		val r = requestLLMStreaming(input)
		val frames = mutableListOf<StreamFrame>()

		r.collect {
			frames.add(it)

			when (it) {
				is StreamFrame.Append -> {
					sink(it.text)
				}
				is StreamFrame.ToolCall -> {}
				is StreamFrame.End -> {}
			}
		}

		onProcessEvent(ProcessEvent.Update(ProcessEntry(rootId, "Parsing/Reasoning", done = true)))

		val reply = frames.toAssistantMessageOrNull()
		if(reply != null) {
			return reply.content
		}

		val toolCalls = frames.toToolCallMessages()
		assert(toolCalls.size == 1) {
			"Expected one tool call, got ${toolCalls.size}"
		}

		val currentToolCall = toolCalls[0]

		val response = resolveCall(currentToolCall)

		return response
	}

	private suspend fun AIAgentFunctionalContext.resolveCall(tc: Message.Tool.Call): String {
		println("Resolving tool call: ${tc.tool} (${tc.content})")

		val id = processCounter++
		onProcessEvent(ProcessEvent.Update(ProcessEntry(id, "Call tool ${tc.tool}: ${tc.content}")))

		llm.writeSession {
			updatePrompt {
				tool {
					call(tc)
				}
			}
		}

		val toolResult = executeTool(tc)

		val out = llm.writeSession {
			updatePrompt {
				tool {
					result(toolResult.toMessage())
				}
			}
			val stream = requestLLMStreaming()
			val f2 = mutableListOf<StreamFrame>()
			stream.collect {
				if(it is StreamFrame.Append) {
					sink(it.text)
				}
				f2.add(it)
			}
			f2
		}

		onProcessEvent(ProcessEvent.Update(ProcessEntry(id, "Call tool ${tc.tool}: ${tc.content}", done = true)))

		val tools = out.toToolCallMessages()
		return if(tools.isNotEmpty()) {
			assert(tools.size == 1) {
				"Expected at most one tool call, got ${tools.size}"
			}
			resolveCall(tools[0])
		} else {
			out.toAssistantMessageOrNull()?.content
				?: throw IllegalStateException("Expected an assistant message or a tool call, got nothing")
		}
	}
}