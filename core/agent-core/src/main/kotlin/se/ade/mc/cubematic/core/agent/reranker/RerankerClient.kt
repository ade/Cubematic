package se.ade.mc.cubematic.core.agent.reranker

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import se.ade.mc.cubematic.core.agent.CustomLlamaModel
import kotlin.math.exp

/** Llama.cpp client */
class RerankerClient(val llamaEndpoint: String) {
	private val apiPath = "/v1/rerank"

	val httpClient = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
			})
		}
	}
	suspend fun rank(query: String, candidates: List<String>): Result<RerankerResponse> {
		return runCatching {
			httpClient.post {
				url("$llamaEndpoint$apiPath")
				contentType(ContentType.Application.Json)
				setBody(
					RerankerRequest(
						model = CustomLlamaModel.default.id,
						query = query,
						documents = candidates
					)
				)
			}
		}.map {
			val ret = it.body<RerankerResponse>()

			ret.copy(results = ret.results.map {
				it.copy(score = sigmoid(it.score))
			})
		}
	}
}

private fun sigmoid(x: Double): Double {
	return 1.0 / (1.0 + exp(-x))
}