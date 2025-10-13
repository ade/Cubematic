package se.ade.mc.cubematic.agent.main

data class AgentState(
	val process: AgentProcess? = AgentProcess(listOf(ProcessEntry(0, "Enter your query", true))),
	val text: String = "Hello world"
)

data class AgentProcess(
	val entries: List<ProcessEntry>
)

data class ProcessEntry(
	val id: Int,
	val text: String,
	val done: Boolean = false
)

sealed interface ProcessEvent {
	data class Update(val processEntry: ProcessEntry): ProcessEvent
}