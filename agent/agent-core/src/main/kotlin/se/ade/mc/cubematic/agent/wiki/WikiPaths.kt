package se.ade.llmtest.core.wiki

import se.ade.kuri.UriProvider
import se.ade.kuri.UriTemplate
import se.ade.kuri.Query

@UriProvider
interface WikiPaths {
	@UriTemplate("api.php")
	fun search(
		@Query("srsearch") query: String,
		@Query action: String = "query",
		@Query list: String = "search",
		@Query format: String = "json"): String

	@UriTemplate("api.php")
	fun pageContent(
		@Query("titles") title: String,
		@Query action: String = "query",
		@Query prop: String = "revisions",
		@Query("rvprop") rvProp: String = "content",
		@Query format: String = "json"
	): String

	@UriTemplate("api.php")
	fun pageExtract(
		@Query("titles") title: String,
		@Query action: String = "query",
		@Query prop: String = "extracts",
		@Query format: String = "json",
		@Query("exintro") exIntro: Boolean = true,
		@Query("explaintext") exPlainText: Boolean = true
	): String
}