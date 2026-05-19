package se.ade.mc.cubematic.core.agent.main

import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import kotlin.time.Clock
import se.ade.mc.cubematic.core.agent.config.InferenceProvider
import se.ade.mc.cubematic.core.agent.rags.DefaultRagClient
import se.ade.mc.cubematic.core.agent.rags.RagClient

class CubeAgentConvo(private val inferenceProvider: InferenceProvider) {
	val ragClient: RagClient = DefaultRagClient()
	val history = mutableListOf<Message>()

	suspend fun query(
		message: String,
		context: QueryContext,
		sink: (ProcessEvent) -> Unit
	): Result<String> {
		val startTime = Clock.System.now()

		/*
		val ragResult = if(history.isEmpty()) ragClient.ragQuery(message).fold(
			onSuccess = { it },
			onFailure = { return "Error querying RAG server: ${it.message}" }
		) else null
		*/

		val agent = MainAgent(history, context, null, inferenceProvider, sink)

		val response = runCatching {
			agent.agent.run(message)
		}.getOrElse {
			return Result.failure(Exception("Failed agent run", it))
		}

		history.add(Message.User(message, RequestMetaInfo(startTime)))
		history.add(Message.Assistant(response, ResponseMetaInfo(Clock.System.now())))

		return Result.success(response)
	}
}