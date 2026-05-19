package se.ade.mc.cubematic.core.agent.wiki

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikiExportResponse(
	val query: Query
) {
	@Serializable
	data class Query(
		val pages: Map<String, PageInfo>,
		val export: Export
	)

	@Serializable
	data class PageInfo(
		val pageid: Int,
		val ns: Int,
		val title: String
	)

	@Serializable
	data class Export(
		@SerialName("*")
		val exportXmlString: String
	)
}