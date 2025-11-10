package se.ade.mc.cubematic.agent.rags

import se.ade.kuri.UriProvider
import se.ade.kuri.UriTemplate

@UriProvider
interface RagServerPaths {
	@UriTemplate("/wiki/{pageName}")
	fun getPage(pageName: String): String

	@UriTemplate("/embeddings/query")
	fun ragQuery(): String
}