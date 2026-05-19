package se.ade.mc.cubematic.core.agent.config

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import se.ade.mc.cubematic.core.agent.config.InferenceConfig.Companion.defaultCapabilities
import kotlin.collections.buildList

@Serializable
data class InferenceConfig(
	val providers: List<InferenceProviderConfig> = listOf(),
) {
	companion object {
		val defaultCapabilities = InferenceModelConfig()
	}
}

@Serializable
data class InferenceModelConfig(
	val id: String = "default",
	val contextLength: Int = 128_000,
	val toolCalling: Boolean = true,
) {
	fun asLlmCapabilities(): List<LLMCapability> {
		return buildList {
			add(LLMCapability.Temperature)
			add(LLMCapability.Schema.JSON.Standard)
			add(LLMCapability.Speculation)
			add(LLMCapability.Completion)
			add(LLMCapability.Thinking)

			if(toolCalling) {
				add(LLMCapability.Tools)
				add(LLMCapability.ToolChoice)
			}
		}
	}
}

@Serializable
sealed interface InferenceProviderConfig {
	@Serializable
	@SerialName("openrouter")
	data class OpenRouterInferenceConfig(
		val apiKey: String,
		val model: InferenceModelConfig = defaultCapabilities
	): InferenceProviderConfig

	@Serializable
	@SerialName("openai")
	data class OpenAIInferenceConfig(
		val apiKey: String,
		val baseUrl: String,
		val model: InferenceModelConfig = defaultCapabilities,
	): InferenceProviderConfig

	fun asInferenceProvider(): InferenceProvider {
		when(val t = this) {
			is OpenRouterInferenceConfig -> {
				return InferenceProvider(
					model = LLModel(
						provider = LLMProvider.OpenRouter,
						id = this.model.id,
						capabilities = model.asLlmCapabilities(),
						contextLength = model.contextLength.toLong(),
					),
					executor = MultiLLMPromptExecutor(
						OpenRouterLLMClient(this.apiKey)
					)
				)
			}
			is OpenAIInferenceConfig -> {
				return InferenceProvider(
					model = LLModel(
						provider = LLMProvider.OpenAI,
						id = "<placeholder>",
						capabilities = model.asLlmCapabilities() + LLMCapability.OpenAIEndpoint.Completions,
						contextLength = 128_000
					),
					executor = MultiLLMPromptExecutor(
						OpenAILLMClient(
							apiKey = this.apiKey,
							settings = OpenAIClientSettings(
								baseUrl = this.baseUrl,
							),
						)
					)
				)
			}
		}
	}
}