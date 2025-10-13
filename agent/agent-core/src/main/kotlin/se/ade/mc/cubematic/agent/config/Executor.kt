package se.ade.mc.cubematic.agent.config

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.executor.llms.all.simpleOpenRouterExecutor

object Executor {
	val openRouter by lazy {
		simpleOpenRouterExecutor(ApiKeys.openRouter)
	}
	val workstation by lazy {
		SingleLLMPromptExecutor(
			OpenAILLMClient(
				apiKey = ApiKeys.workstation,
				settings = OpenAIClientSettings(
					baseUrl = "http://192.168.10.50:10000"
				),
			)
		)
	}
	val localOAI by lazy {
		SingleLLMPromptExecutor(
			OpenAILLMClient(
				apiKey = "None",
				settings = OpenAIClientSettings(
					baseUrl = "http://127.0.0.1:8080",
				),
			)
		)
	}
}