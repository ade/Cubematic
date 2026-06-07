package se.ade.mc.cubematic.core.agent.wiki

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig

class CoreWikiClient {
	private val client = HttpClient {
		install(ContentNegotiation) {
			json(Json {
				ignoreUnknownKeys = true
			})
		}
	}

	val xmlSerializer = XML {
		defaultPolicy {
			// Drop unknown elements and attributes instead of throwing an error
			unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
		}
	}

	private val pathBuilder: WikiPaths = KuriWikiPaths

	private val baseUrl = "https://minecraft.wiki/"

	suspend fun getPageExtract(pageTitle: String): String? {
		val url = "$baseUrl${KuriWikiPaths.pageExtract(title = pageTitle)}"
		//println("Fetching URL: $url")
		val response: PageExtractResponse = client.get(url).body()
		val page = response.query.pages.values.firstOrNull()
		return page?.extract
	}

	suspend fun getPageContent(pageTitle: String): String? {
		val url = "$baseUrl${KuriWikiPaths.pageContent(title = pageTitle)}"
		//println("Fetching URL: $url")
		val response: PageContentResponse = client.get(url).body()
		val page = response.query.pages.values.firstOrNull()

		if(page?.pageid == -1 || page?.revisions.isNullOrEmpty())
			return null // Page was missing

		return page.revisions.firstOrNull()?.content
	}

	suspend fun getRawPageContent(pageTitle: String): String? {
		val url = "$baseUrl${KuriWikiPaths.pageRawContent(title = pageTitle)}"
		//println("Fetching URL: $url")
		val response: String = client.get(url).bodyAsText()
		return response
	}

	suspend fun searchPages(query: String): List<String> {
		val url = "$baseUrl${KuriWikiPaths.search(query = query)}"
		//println("Fetching URL: $url")
		val response: SearchResponse = client.get(url).body()
		return response.query.search.map { it.title }
	}

	suspend fun allPages(namespace: Int? = null, continueFrom: String? = null): WikiAllPagesResponse {
		val url = pathBuilder.allPages(
			limit = 500,
			continueToken = continueFrom,
			namespace = namespace
		)
		return client.get("${baseUrl}$url").body()
	}

	suspend fun getRootCategories(
		limit: Int = 500,
		properties: String = "size",
		minMembers: Int? = null,
		continueToken: String? = null
	): WikiAllCategoriesResponse {
		val url = pathBuilder.allCategories(
			minMembers = minMembers,
			properties = properties,
			limit = limit,
			continueToken = continueToken
		)
		return client.get("${baseUrl}$url").body()
	}

	suspend fun getCategoryMembers(
		categoryTitle: String,
		limit: Int = 500,
		memberType: String? = null,
		continueToken: String? = null
	): WikiCategoryMembersResponse {
		val url = pathBuilder.categoryMembers(
			categoryTitle = categoryTitle,
			limit = limit,
			memberType = memberType,
			continueToken = continueToken
		)
		return client.get("${baseUrl}$url").body()
	}

	/**
	 * Export pages, using the export api function (XML).
	 *
	 * @param titles List of page titles to export (max 50)
	 * @return Pair of root WikiExportResponse containing metadata and unparsed xml, and parsed ExportedPageXml
	 */
	suspend fun exportPages(titles: List<String>): Pair<WikiExportResponse, ExportedPageXml> {
		if(titles.size > 500)
			throw IllegalArgumentException("Cannot export more than 50 pages at once")

		val url = pathBuilder.exportPages(titles = titles.joinToString("|"))
		val json: WikiExportResponse = client.get("${baseUrl}$url").body()
		val xml = xmlSerializer.decodeFromString<ExportedPageXml>(json.query.export.exportXmlString)
		return json to xml
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
	val pageid: Int? = -1,
	val ns: Int,
	val title: String,
	val revisions: List<Revision>? = null
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