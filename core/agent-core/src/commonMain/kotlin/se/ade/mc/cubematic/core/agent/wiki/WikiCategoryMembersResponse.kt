package se.ade.mc.cubematic.core.agent.wiki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
Example response from MediaWiki API for categorymembers:
{
  "batchcomplete": "",
  "continue": {
    "cmcontinue": "page|...",
    "continue": "-||"
  },
  "query": {
    "categorymembers": [
      {
        "pageid": 12345,
        "ns": 0,
        "title": "Example Page"
      },
      ...
    ]
  }
}
 */

@Serializable
data class WikiCategoryMembersResponse(
	val batchcomplete: String? = null,
	@SerialName("continue")
	val continueInfo: ContinueInfo? = null,
	val query: Query
) {
	@Serializable
	data class Query(
		val categorymembers: List<Member>
	)

	@Serializable
	data class Member(
		val pageid: Int,
		val ns: Int,
		val title: String
	)

	@Serializable
	data class ContinueInfo(
		val cmcontinue: String,
		@SerialName("continue")
		val continue2: String? = null
	)
}

