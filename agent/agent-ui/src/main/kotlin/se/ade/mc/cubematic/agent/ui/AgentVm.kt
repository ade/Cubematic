package se.ade.mc.cubematic.agent.ui

import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import se.ade.mc.cubematic.agent.config.InferenceProvider
import se.ade.mc.cubematic.agent.main.AgentProcess
import se.ade.mc.cubematic.agent.main.AgentState
import se.ade.mc.cubematic.agent.main.MainAgent
import se.ade.mc.cubematic.agent.main.ProcessEvent

class AgentVm: ViewModel() {
	val state = MutableStateFlow(AgentState())
	fun submit(t: String) {
		viewModelScope.launch { queryAgent(t) }
	}

	private val history = mutableListOf<Message>()

	private suspend fun queryAgent(text: String) {
		state.update {
			it.copy(text = "", process = null)
		}
		val startTime = Clock.System.now()
		val result = MainAgent(history,
			provider = InferenceProvider.local,
			sink = { t ->
				state.update { it.copy(text = it.text + t) }
			},
			onProcessEvent = {
				onProcessEvent(it)
			}
		).agent.run(text)

		history.add(Message.User(text, RequestMetaInfo(startTime)))
		history.add(Message.Assistant(result, ResponseMetaInfo(Clock.System.now())))
		state.update {
			it.copy(text = result)
		}
	}

	private fun onProcessEvent(event: ProcessEvent) {
		when(event) {
			is ProcessEvent.Update -> {
				state.update { st ->
					val current = st.process?.entries?.toMutableList() ?: mutableListOf()
					val idx = current.indexOfFirst { it.id == event.processEntry.id }
					if(idx >= 0) {
						current[idx] = event.processEntry
					} else {
						current.add(event.processEntry)
					}
					st.copy(process = AgentProcess(current))
				}
			}
		}
	}
}