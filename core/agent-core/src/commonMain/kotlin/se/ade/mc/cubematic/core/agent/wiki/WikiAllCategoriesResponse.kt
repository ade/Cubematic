package se.ade.mc.cubematic.core.agent.wiki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*

{
  "batchcomplete": "",
  "continue": {
    "accontinue": "Hanging_roots_sounds",
    "continue": "-||"
  },
  "query": {
    "allcategories": [
      {
        "size": 151,
        "pages": 0,
        "files": 151,
        "subcats": 0,
        "*": "1.8 development flower pot renders"
      },
		...
 */

@Serializable
data class WikiAllCategoriesResponse(
	val batchcomplete: String? = null,
	@SerialName("continue")
	val continueInfo: ContinueInfo? = null,
	val query: Query
) {

	@Serializable
	data class Query(
		val allcategories: List<Entry>
	)

	@Serializable
	data class Entry(
		val size: Int,
		val pages: Int,
		val files: Int,
		val subcats: Int,
		@SerialName("*")
		val title: String
	)

	@Serializable
	data class ContinueInfo(
		val accontinue: String,
	)
}
