package se.ade.mc.cubematic.core.agent.config

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLModel

data class InferenceProvider(
	val model: LLModel,
	val executor: MultiLLMPromptExecutor
)