package se.ade.mc.cubematic.core.agent.rags

import kotlinx.serialization.Serializable

@Serializable
data class QueryRequest(
	val query: String,
	val topK: Int = 10
)

@Serializable
data class QueryResponse(
	val chunks: List<ResponseChunk>,
	val query: String,
	val resultCount: Int
)

@Serializable
data class ResponseChunk(
	val pageName: String,
	val content: String,
	val breadcrumbs: List<String>,
)
