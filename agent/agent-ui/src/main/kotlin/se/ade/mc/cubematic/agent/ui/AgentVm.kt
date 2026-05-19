package se.ade.mc.cubematic.agent.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.ade.mc.cubematic.core.agent.main.*

/*
Context: QueryContext(playerName=ay_dizzle, playerLevel=0, health=20, foodLevel=20, location=LocationContext(worldName=world, x=4, y=65, z=0), time=dawn (30 seconds until morning), nearbyEntities=[], inventoryItems=[InventoryItem(type=COMPASS, quantity=1), InventoryItem(type=ACACIA_BUTTON, quantity=5)], gameMode=SURVIVAL)
 */

val fakeContext = QueryContext(
	serverInfo = ServerInfo(version = "1.21.6"),
	playerName = "Steve",
	playerLevel = 5,
	health = 20,
	foodLevel = 20,
	location = QueryContext.LocationContext(
		worldName = "world",
		x = 100,
		y = 64,
		z = 100,
	),
	time = "noon",
	nearbyEntities = listOf("COW"),
	inventoryItems = listOf(
		QueryContext.InventoryItem(type = "WOODEN_SWORD", quantity = 1),
		QueryContext.InventoryItem(type = "APPLE", quantity = 5),
	),
	gameMode = "SURVIVAL"
)

class AgentVm: ViewModel() {
	private val config = loadConfig()

	val state = MutableStateFlow(AgentUiState())
	fun submit(t: String) {
		viewModelScope.launch { queryAgent(t) }
	}

	private val agent = CubeAgent(
		config.inferenceConfig.providers.first().asInferenceProvider()
	)

	private val convo = agent.createConvo()

	private suspend fun queryAgent(text: String) {
		// Append a new streaming entry for this query
		state.update { st ->
			st.copy(entries = st.entries + ChatEntry(query = text, isStreaming = true))
		}

		val result = convo.query(message = text, context = fakeContext) {
			onProcessEvent(it)
		}.getOrElse {
			state.update { st ->
				val list = st.entries.toMutableList()
				val last = st.entries.lastOrNull()
				if(last != null) {
					list[list.lastIndex] = last.copy(response = "Error: ${it.message}", isStreaming = false, process = null)
				}
				st.copy(entries = list)
			}
			return
		}

		// Mark the last entry as done with the final response
		state.update { st ->
			val updated = st.entries.toMutableList()
			val last = updated.lastOrNull()
			if (last != null) {
				updated[updated.lastIndex] = last.copy(response = result, isStreaming = false, process = null)
			}
			st.copy(entries = updated)
		}
	}

	private fun onProcessEvent(event: ProcessEvent) {
		when(event) {
			is ProcessEvent.TextSink -> {
				state.update { st ->
					val updated = st.entries.toMutableList()
					val last = updated.lastOrNull() ?: return@update st
					updated[updated.lastIndex] = last.copy(response = last.response + event.text)
					st.copy(entries = updated)
				}
			}
			is ProcessEvent.Update -> {
				state.update { st ->
					val updated = st.entries.toMutableList()
					val last = updated.lastOrNull() ?: return@update st
					val current = last.process?.entries?.toMutableList() ?: mutableListOf()
					val idx = current.indexOfFirst { it.id == event.processEntry.id }
					if(idx >= 0) {
						current[idx] = event.processEntry
					} else {
						current.add(event.processEntry)
					}
					updated[updated.lastIndex] = last.copy(process = AgentProcess(current))
					st.copy(entries = updated)
				}
			}

			is ProcessEvent.ProgressMessage -> {
				state.update { st ->
					val updated = st.entries.toMutableList()
					val last = updated.lastOrNull() ?: return@update st
					val current = last.process?.entries ?: emptyList()
					val newProcess = AgentProcess(
						current + ProcessEntry(id = -1, text = event.message, done = true)
					)
					updated[updated.lastIndex] = last.copy(process = newProcess)
					st.copy(entries = updated)
				}
			}
		}
	}
}