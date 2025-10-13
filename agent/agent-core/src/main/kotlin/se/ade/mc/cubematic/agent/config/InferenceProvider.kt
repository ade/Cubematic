package se.ade.mc.cubematic.agent.config

import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel

data class InferenceProvider(
	val model: LLModel,
	val executor: SingleLLMPromptExecutor
) {
	companion object Companion {
		val local by lazy {
			InferenceProvider(
				model = CustomLlamaModel.default,
				executor = Executor.workstation
			)
		}
	}
}