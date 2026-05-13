package se.ade.mc.cubematic.core.agent.rags

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DefaultRagClient(val baseUrl: String = "http://192.168.10.50:8080"): RagClient {
	val uriProvider: RagServerPaths = KuriRagServerPaths

	private val httpClient = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
			})
		}
	}

	override suspend fun getPage(pageName: String): Result<String> {
		httpClient.get {
			url("$baseUrl${uriProvider.getPage(pageName)}")
		}.body<String>().let {
			return Result.success(it)
		}
	}

	override suspend fun ragQuery(query: String, topK: Int): Result<QueryResponse> {
		httpClient.post {
			setBody(QueryRequest(query, topK))
			contentType(ContentType.Application.Json)
			url("$baseUrl${uriProvider.ragQuery()}")
		}.body<QueryResponse>().let {
			return Result.success(it)
		}
	}
}