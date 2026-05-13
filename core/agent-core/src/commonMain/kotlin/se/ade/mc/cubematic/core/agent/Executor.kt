package se.ade.mc.cubematic.core.agent

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor
import se.ade.mc.cubematic.core.agent.config.ApiKeys

object Executor {
	val workStationLLmClient = OpenAILLMClient(
		apiKey = ApiKeys.workstation,
		settings = OpenAIClientSettings(
			baseUrl = "http://192.168.10.50:10000"
		),
	)
	val workStationLLmClient2 = OpenAILLMClient(
		apiKey = ApiKeys.workstation,
		settings = OpenAIClientSettings(
			baseUrl = "http://192.168.10.50:10001"
		),
	)

	val openRouter by lazy {
		simpleOpenRouterExecutor(ApiKeys.openRouter)
	}
	val workstation = SingleLLMPromptExecutor(
		workStationLLmClient
	)
	val localOAI = SingleLLMPromptExecutor(
		OpenAILLMClient(
			apiKey = "None",
			settings = OpenAIClientSettings(
				baseUrl = "http://127.0.0.1:8080",
			),
		)
	)
}