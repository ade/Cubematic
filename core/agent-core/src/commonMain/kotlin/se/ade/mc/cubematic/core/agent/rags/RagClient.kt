package se.ade.mc.cubematic.core.agent.rags

interface RagClient {
	suspend fun getPage(pageName: String): Result<String>
	suspend fun ragQuery(query: String, topK: Int = 10): Result<QueryResponse>
}