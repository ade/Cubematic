package se.ade.mc.cubematic.agent.ui

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.RequestMetaInfo
import ai.koog.prompt.message.ResponseMetaInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import se.ade.mc.cubematic.agent.config.CustomLlamaModel
import se.ade.mc.cubematic.agent.config.InferenceProvider
import se.ade.mc.cubematic.agent.main.AgentProcess
import se.ade.mc.cubematic.agent.main.AgentState
import se.ade.mc.cubematic.agent.main.CubeAgent
import se.ade.mc.cubematic.agent.main.CubeAgentConvo
import se.ade.mc.cubematic.agent.main.MainAgent
import se.ade.mc.cubematic.agent.main.ProcessEntry
import se.ade.mc.cubematic.agent.main.ProcessEvent
import se.ade.mc.cubematic.agent.main.QueryContext
import se.ade.mc.cubematic.agent.main.ServerInfo

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

	val state = MutableStateFlow(AgentState())
	fun submit(t: String) {
		viewModelScope.launch { queryAgent(t) }
	}

	private val agent = CubeAgent(InferenceProvider(
		model = CustomLlamaModel.default,
		executor = SingleLLMPromptExecutor(
			OpenAILLMClient(
				apiKey = config.apiKey,
				settings = OpenAIClientSettings(
					baseUrl = config.baseUrl
				),
			)
		)
	))

	private val history = mutableListOf<Message>()
	private val convo = agent.createConvo()

	private suspend fun queryAgent(text: String) {
		state.update {
			it.copy(text = "", process = null)
		}

		val result = convo.query(message = text, context = fakeContext) {
			onProcessEvent(it)
		}

		state.update {
			it.copy(text = result)
		}
	}

	private fun onProcessEvent(event: ProcessEvent) {
		when(event) {
			is ProcessEvent.TextSink -> {
				state.update { st ->
					st.copy(
						text = st.text + event.text
					)
				}
			}
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

			is ProcessEvent.ProgressMessage -> {
				val process = state.value.process?.entries ?: emptyList()
				state.update {
					it.copy(process = it.process?.copy(
						entries = process + ProcessEntry(
							id = -1,
							text = event.message,
							done = true
						)
					))
				}
			}
		}
	}
}