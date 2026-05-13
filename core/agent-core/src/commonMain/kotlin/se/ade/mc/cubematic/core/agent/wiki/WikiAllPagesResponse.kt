package se.ade.mc.cubematic.core.agent.wiki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiAllPagesResponse(
	@SerialName("continue")
	val continueInfo: Continue? = null,

	val query: Query
) {
	@Serializable
	data class Continue(
		val apcontinue: String,
		@SerialName("continue")
		val continue2: String
	)

	@Serializable
	data class Query(
		val allpages: List<Page>
	)

	@Serializable
	data class Page(
		val pageid: Int,
		val ns: Int,
		val title: String
	)
}

