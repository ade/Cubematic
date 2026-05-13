package se.ade.mc.cubematic.core.agent.config

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

object CustomLlamaModel {
	val default = LLModel(
		provider = LLMProvider.OpenAI,
		id = "<placeholder>",
		capabilities = listOf(
			LLMCapability.Temperature,
			LLMCapability.Schema.JSON.Standard,
			LLMCapability.Speculation,
			LLMCapability.Completion,
			LLMCapability.Tools,
			LLMCapability.OpenAIEndpoint.Completions
		),
		contextLength = 128_000
	)
}