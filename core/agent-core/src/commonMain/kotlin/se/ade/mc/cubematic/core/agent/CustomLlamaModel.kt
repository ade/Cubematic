package se.ade.mc.cubematic.core.agent

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
		contextLength = 22_000
	)
	val embedder = LLModel(
		provider = LLMProvider.OpenAI,
		id = "n/a",
		capabilities = listOf(
			LLMCapability.Embed,
		),
		contextLength = 32_000
	)
}