package se.ade.mc.cubematic.core.agent.config

import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

private val standardCapabilities: List<LLMCapability> = listOf(
	LLMCapability.Temperature,
	LLMCapability.Schema.JSON.Standard,
	LLMCapability.Speculation,
	LLMCapability.Completion
)

private val tools: List<LLMCapability> = listOf(
	LLMCapability.Tools,
	LLMCapability.ToolChoice,
)

object CustomOpenRouterModel {
	val nemotron7b = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "nvidia/nemotron-nano-9b-v2:free",
		capabilities = standardCapabilities + tools,
		contextLength = 128_000
	)
	val gpt_oss_20b = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "openai/gpt-oss-20b:free",
		capabilities = standardCapabilities,
		contextLength = 131_072
	)

	val tongyi30b = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "alibaba/tongyi-deepresearch-30b-a3b:free",
		capabilities = standardCapabilities + tools,
		contextLength = 131_072
	)

	/** Extremely good, but not very fast */
	val DeepSeekProverV2_671B = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "deepseek/deepseek-prover-v2:free",
		capabilities = standardCapabilities,
		contextLength = 163_840
	)

	val Dolphin3_Mistral24B = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "cognitivecomputations/dolphin3.0-mistral-24b:free",
		capabilities = standardCapabilities,
		contextLength = 32_768
	)
	/** High quality, high speed - Best so far */
	val MetaLlama3_3_Instruct70B = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "meta-llama/llama-3.3-70b-instruct:free", // Works good
		//id = "deepseek/deepseek-chat-v3-0324:free",
		capabilities = standardCapabilities + tools,
		contextLength = 65_536
	)
	/* Works reasonably well and is fast */
	val MetaLlama3_3_Instruct8B = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "meta-llama/llama-3.3-8b-instruct:free", // Works good
		capabilities = standardCapabilities + tools,
		contextLength = 128_000
	)
	/** Fails too often */
	val MetaLlama3_2_Instruct3B = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "meta-llama/llama-3.2-3b-instruct:free", // Works good
		capabilities = standardCapabilities + tools,
		contextLength = 131_072
	)
	/** Pretty bad results */
	val MetaLlama4Scout = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "meta-llama/llama-4-scout:free",
		capabilities = standardCapabilities,
		contextLength = 128_000
	)
	/** outputs a ton of reasoning text, doesnt follow output format instructions */
	val Phi4ReasoningPlus = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "microsoft/phi-4-reasoning-plus:free",
		capabilities = standardCapabilities,
		contextLength = 32_768
	)
	/** Not very good */
	val Mistral7BInstruct = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "mistralai/mistral-7b-instruct:free",
		capabilities = standardCapabilities + tools,
		contextLength = 32_768
	)

	/** Very decent results */
	@Deprecated("3.2 available")
	val MistralSmall_3_1_24b_Instruct = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "mistralai/mistral-small-3.1-24b-instruct:free",
		capabilities = standardCapabilities,
		contextLength = 128_000
	)

	val MistralSmall_3_2_24b_Instruct = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "mistralai/mistral-small-3.2-24b-instruct:free",
		capabilities = standardCapabilities,
		contextLength = 131_072
	)



	/** Works ok, not tested much */
	val MistralDevstralSmall = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "mistralai/devstral-small:free",
		capabilities = standardCapabilities,
		contextLength = 128_000
	)
	/** Outputs, <think> tags, so not usable without tweaks */
	val DeepHermes3_Mistral24b = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "nousresearch/deephermes-3-mistral-24b-preview:free",
		capabilities = standardCapabilities,
		contextLength = 32_768
	)
	/** untested */
	val Qwen2_5_72BInstruct = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "qwen/qwen-2.5-72b-instruct:free",
		capabilities = standardCapabilities + tools,
		contextLength = 32_768
	)

	/** High quality. Not very fast. */
	val DeepSeekV3_0324 = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "deepseek/deepseek-chat-v3-0324:free",
		capabilities = standardCapabilities,
		contextLength = 163_840
	)
	/** Very, very slow */
	val DeepSeekR1_0528 = LLModel(
		provider = LLMProvider.OpenRouter,
		id = "deepseek/deepseek-r1-0528:free",
		capabilities = standardCapabilities,
		contextLength = 163_840
	)
}