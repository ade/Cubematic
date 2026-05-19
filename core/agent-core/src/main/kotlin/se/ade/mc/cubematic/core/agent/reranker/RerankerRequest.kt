package se.ade.mc.cubematic.core.agent.reranker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RerankerRequest(
	val model: String,
	val query: String,
	val documents: List<String>,
	@SerialName("top_n")
	val amountTopResults: Int = 5
)