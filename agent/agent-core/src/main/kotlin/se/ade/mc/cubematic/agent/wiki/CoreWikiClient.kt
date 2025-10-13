package se.ade.llmtest.core.wiki

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class CoreWikiClient {
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
			})
		}
	}

	private val baseUrl = "https://minecraft.wiki/"

	suspend fun getPageExtract(pageTitle: String): String? {
		val url = "$baseUrl${KuriWikiPaths.pageExtract(title = pageTitle)}"
		println("Fetching URL: $url")
		val response: PageExtractResponse = client.get(url).body()
		val page = response.query.pages.values.firstOrNull()
		return page?.extract
	}

	suspend fun getPageContent(pageTitle: String): String? {
		val url = "$baseUrl${KuriWikiPaths.pageContent(title = pageTitle)}"
		println("Fetching URL: $url")
		val response: PageContentResponse = client.get(url).body()
		val page = response.query.pages.values.firstOrNull()
		return page?.revisions?.firstOrNull()?.content
	}

	suspend fun searchPages(query: String): List<String> {
		val url = "$baseUrl${KuriWikiPaths.search(query = query)}"
		println("Fetching URL: $url")
		val response: SearchResponse = client.get(url).body()
		return response.query.search.map { it.title }
	}
}

@Serializable
private data class SearchResponse(
	val query: SearchQuery
)

@Serializable
private data class SearchQuery(
	val search: List<SearchResult>
)

@Serializable
private data class SearchResult(
	val ns: Int,
	val title: String,
	val pageid: Int,
	val size: Int,
	val wordcount: Int,
	val snippet: String,
	val timestamp: String
)

@Serializable
private data class PageContentResponse(
	val query: QueryWithRevisions
)

@Serializable
private data class QueryWithRevisions(
	val pages: Map<String, WikiPageWithRevisions>
)

@Serializable
private data class WikiPageWithRevisions(
	val pageid: Int,
	val ns: Int,
	val title: String,
	val revisions: List<Revision>
)

@Serializable
private data class Revision(
	@SerialName("*")
	val content: String
)

@Serializable
private data class PageExtractResponse(
	val query: Query
)

@Serializable
private data class Query(
	val pages: Map<String, WikiPage>
)

@Serializable
private data class WikiPage(
	val pageid: Int,
	val ns: Int,
	val title: String,
	val extract: String
)