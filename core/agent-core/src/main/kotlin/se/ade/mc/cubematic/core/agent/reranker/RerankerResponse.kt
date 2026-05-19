package se.ade.mc.cubematic.core.agent.reranker

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RerankerResponse(
	val results: List<RankItem>
)

@Serializable
data class RankItem(
	val index: Int,
	@SerialName("relevance_score")
	val score: Double
)