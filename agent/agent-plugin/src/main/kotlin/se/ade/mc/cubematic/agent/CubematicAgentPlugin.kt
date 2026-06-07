package se.ade.mc.cubematic.agent

import io.papermc.paper.event.player.ChatEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.config.configProvider
import se.ade.mc.cubematic.core.agent.config.InferenceConfig
import se.ade.mc.cubematic.core.agent.config.InferenceModelConfig
import se.ade.mc.cubematic.core.agent.config.InferenceProviderConfig
import se.ade.mc.cubematic.core.agent.main.CubeAgent
import se.ade.mc.cubematic.core.agent.main.CubeAgentConvo
import se.ade.mc.cubematic.core.agent.main.ProcessEvent
import se.ade.mc.cubematic.core.agent.main.QueryContext
import se.ade.mc.cubematic.core.agent.main.ServerInfo
import se.ade.mc.cubematic.core.agent.utils.WorldTimeFormat
import se.ade.mc.cubematic.extensions.commands
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val MAX_CHAT_HISTORY = 10

@Serializable
data class AgentConfig(
	val apiKey: String = "",
	val baseUrl: String = "",
	val inferenceConfig: InferenceConfig = InferenceConfig(
		providers = listOf(
			InferenceProviderConfig.OpenAIInferenceConfig(
				apiKey = "example",
				baseUrl = "example",
				model = InferenceModelConfig(
					id = "default",
					contextLength = 128_000,
				)
			)
		)
	),
)

@Suppress("UnstableApiUsage")
class CubematicAgentPlugin: JavaPlugin() {
	var config by configProvider { AgentConfig() }
	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
	private var globalConvo: Pair<Instant, CubeAgentConvo>? = null
	private val convoMap = mutableMapOf<UUID, CubeAgentConvo>()
	private val cubeAgent = CubeAgent(
		config.inferenceConfig.providers.first().asInferenceProvider()
	)
	private val chatHistory: MutableList<Pair<String, String>> = mutableListOf()

	override fun onEnable() {
		logger.info("Cubematic Agent Plugin enabled")
		server.pluginManager.registerEvents(eventHandler, this)

		commands {
			command("qb") {
				command("ask") {
					withPlayer {
						greedyString("question") { ctx, s, player ->
							val context = player.queryContext()
							//logger.info("Context: $context")

							scope.launch {
								val uuid = player.uniqueId
								val convo = cubeAgent.createConvo().also {
									convoMap[uuid] = it
								}

								val resp = convo.query(s, context) {
									if(it is ProcessEvent.ProgressMessage)
										player.sendMessage(it.message)
								}.getOrElse {
									player.sendMessage("An error occurred: ${it.message}")
									return@launch
								}

								player.qbSendMessage(resp)
							}

							player.sendMessage("Loading...")
						}
					}
				}
				command("re") {
					withPlayer {
						greedyString("response") { ctx, s, player ->
							val playerContext = player.queryContext()
							scope.launch {
								convoMap.getOrPut(player.uniqueId) { cubeAgent.createConvo() }
									.query(s, playerContext) {
										// Lambda (partial text response) not used for now
									}
									.getOrElse {
										player.sendMessage("An error occurred: ${it.message}")
										return@launch
									}
									.let { resp ->
										player.qbSendMessage(resp)
									}
							}
							player.sendMessage("Loading...")
						}
					}
				}
			}
		}
	}

	private fun Audience.qbSendMessage(t: String) {
		sendMessage(
			Component.text("<")
				.append(Component.text("QB").color(NamedTextColor.LIGHT_PURPLE))
				.append(Component.text("> "))
				.append(Component.text(t))
		)
	}

	override fun onDisable() {
		logger.info("Cubematic Agent Plugin disabled")
	}

	private val eventHandler = object: Listener {
		@EventHandler
		fun onAsyncChatEvent(event: ChatEvent) {
			val message = event.message().toString()

			if(message.lowercase().contains("@qb")) {
				val player = event.player
				val context = player.queryContext(chatHistory)

				scope.launch {
					val gc = globalConvo

					val convo = if(gc == null || Clock.System.now() - gc.first > 15.minutes) {
						cubeAgent.createConvo().also { c ->
							globalConvo = Clock.System.now() to c
						}
					} else {
						gc.second
					}

					val resp = convo.query(message, context) {
						if(it is ProcessEvent.ProgressMessage)
							server.qbSendMessage("[${it.message}]")
					}.getOrElse {
						server.sendMessage(Component.text("<QB> ").append(Component.text("An error occurred: ${it.message}").color(NamedTextColor.RED)))
						return@launch
					}

					server.qbSendMessage(resp)
				}
			}

			val senderName = event.player.name

			while(chatHistory.size >= MAX_CHAT_HISTORY) {
				chatHistory.removeAt(0)
			}

			chatHistory.add(senderName to message)
		}
	}

	private fun Player.queryContext(chatHistory: List<Pair<String, String>> = emptyList()): QueryContext {
		val entities = location.getNearbyEntities(16.0,16.0,16.0)
			.filter { it.type != EntityType.PLAYER }
			.groupBy { it.type.name + if(it is Item) { ":" + it.itemStack.type.name } else "" }
			.map { (type, ents) ->
				val amount = ents.size
				if(amount == 1) {
					type
				} else {
					"$type (x$amount)"
				}
			}
			.take(16)

		return QueryContext(
			serverInfo = ServerInfo(
				version = server.version
			),
			playerName = name,
			playerLevel = level,
			health = health.toInt(),
			foodLevel = foodLevel,
			location = QueryContext.LocationContext(
				worldName = location.world?.name ?: "unknown",
				x = location.blockX,
				y = location.blockY,
				z = location.blockZ
			),
			time = getTimeString(),
			nearbyEntities = entities,
			inventoryItems = inventory.contents.filterNotNull().groupBy { it.type }.map { (type, items) ->
				QueryContext.InventoryItem(type.key.toString(), items.sumOf { it.amount })
			},
			gameMode = gameMode.name,
			chatHistory = chatHistory,
		)
	}

	private fun getTimeString(): String {
		val world = server.worlds.firstOrNull() ?: return "unknown time"
		val dayTime = world.time
		return WorldTimeFormat(dayTime).getTimeString()
	}
}