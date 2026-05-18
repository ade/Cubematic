package se.ade.mc.cubematic.agent.ui

import se.ade.mc.cubematic.core.agent.main.AgentProcess

data class ChatEntry(
    val query: String,
    val process: AgentProcess? = null,
    val response: String = "",
    val isStreaming: Boolean = false,
)

data class AgentUiState(
    val entries: List<ChatEntry> = emptyList(),
)

