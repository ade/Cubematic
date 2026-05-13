package se.ade.mc.cubematic.core.agent.wiki

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

	@UriTemplate("w/{title}")
	fun pageRawContent(
		title: String,
		@Query action: String = "raw"
	): String

	/**
	 * Get all pages, with optional prefix filtering and continuation,
	 * ONLY in the given namespace which is main by default.
	 *
	 * @param limit Maximum number of pages to return (max 500)
	 * @param prefix Search for all page titles that begin with this value.
	 * @param namespace Only include pages from this namespace (default: null for main namespace).
	 * @param continueToken Continue from this token (for pagination).
	 * @param filterRedir Filter redirects. Values: "all", "redirects", "nonredirects" (default: "nonredirects")
	 * @return The URI for fetching all pages.
	 */
	@UriTemplate("api.php")
	fun allPages(
		@Query action: String = "query",
		@Query list: String = "allpages",
		@Query format: String = "json",
		@Query("aplimit") limit: Int = 10,
		@Query("apprefix") prefix: String? = null,
		@Query("apnamespace") namespace: Int? = null,
		@Query("apcontinue") continueToken: String? = null,
		@Query("apfilterredir") filterRedir: String = "nonredirects"
	): String

	/**
	 * Get all categories, with continuation.
	 *
	 * @param limit Maximum number of categories to return (max 500)
	 * @param continueToken Continue from this token (for pagination).
	 * @param properties Extra properties to include for each category. Values: "size", "hidden" (default: none)
	 * @param minMembers Minimum number of members a category must have to be included (default: 0)
	 * @return The URI for fetching all categories.
	 */
	@UriTemplate("api.php")
	fun allCategories(
		@Query action: String = "query",
		@Query list: String = "allcategories",
		@Query format: String = "json",
		@Query("acmin") minMembers: Int? = 0,
		@Query("acprop") properties: String? = null,
		@Query("aclimit") limit: Int = 10,
		@Query("accontinue") continueToken: String? = null
	): String

	/**
	 * Get members of a category, with continuation.
	 *
	 * @param categoryTitle The title of the category including "Category:" (e.g. "Category:Example")
	 * @param limit Maximum number of members to return (max 500)
	 * @param memberType Type of members to return. Values: "page", "subcat", "file" (default no filter)
	 * @param continueToken Continue from this token (for pagination).
	 * @return The URI for fetching category members.
	 */
	@UriTemplate("api.php")
	fun categoryMembers(
		@Query("cmtitle") categoryTitle: String,
		@Query action: String = "query",
		@Query list: String = "categorymembers",
		@Query format: String = "json",
		@Query("cmlimit") limit: Int = 10,
		@Query("cmtype") memberType: String? = null,
		@Query("cmcontinue") continueToken: String? = null
	): String

	@UriTemplate("api.php")
	fun exportPages(
		@Query titles: String,
		@Query action: String = "query",
		@Query export: Boolean = true,
		@Query format: String = "json"
	): String
}