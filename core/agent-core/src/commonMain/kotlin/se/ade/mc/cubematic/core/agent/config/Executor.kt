package se.ade.mc.cubematic.core.agent.config

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object Executor {
	private val workStationLLmClient = OpenAILLMClient(
		apiKey = ApiKeys.workstation,
		settings = OpenAIClientSettings(
			baseUrl = "http://192.168.10.50:10000"
		),
	)

	val openRouter by lazy {
		simpleOpenRouterExecutor(ApiKeys.openRouter)
	}
	val localOAI by lazy {
		MultiLLMPromptExecutor(
			OpenAILLMClient(
				apiKey = "None",
				settings = OpenAIClientSettings(
					baseUrl = "http://127.0.0.1:8080",
				),
			)
		)
	}
	val workstation = MultiLLMPromptExecutor(
		workStationLLmClient
	)
}