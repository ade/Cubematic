package se.ade.mc.cubematic.agent

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.core.agent.config.CustomLlamaModel
import se.ade.mc.cubematic.core.agent.config.InferenceProvider
import se.ade.mc.cubematic.config.configProvider
import se.ade.mc.cubematic.core.agent.config.InferenceConfig
import se.ade.mc.cubematic.core.agent.config.InferenceModelConfig
import se.ade.mc.cubematic.core.agent.config.InferenceProviderConfig
import se.ade.mc.cubematic.core.agent.main.CubeAgent
import se.ade.mc.cubematic.core.agent.main.CubeAgentConvo
import se.ade.mc.cubematic.core.agent.main.ProcessEvent
import se.ade.mc.cubematic.core.agent.main.QueryContext
import se.ade.mc.cubematic.core.agent.main.ServerInfo
import se.ade.mc.cubematic.extensions.commands
import java.util.UUID

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
	private val convoMap = mutableMapOf<UUID, CubeAgentConvo>()
	private val cubeAgent = CubeAgent(
		config.inferenceConfig.providers.first().asInferenceProvider()
	)

	override fun onEnable() {
		logger.info("Cubematic Agent Plugin enabled")
		server.pluginManager.registerEvents(eventHandler, this)

		commands {
			command("qb") {
				command("ask") {
					withPlayer {
						greedyString("question") { ctx, s, player ->
							val context = player.queryContext()
							logger.info("Context: $context")

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

								player.sendMessage(
									Component.text("<QB> ")
										.append(Component.text(resp).color(NamedTextColor.LIGHT_PURPLE))
								)
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
										player.sendMessage(resp)
									}
							}
							player.sendMessage("Loading...")
						}
					}
				}
			}
		}
	}

	override fun onDisable() {
		logger.info("Cubematic Agent Plugin disabled")
	}

	private val eventHandler = object: Listener {

	}

	private fun Player.queryContext(): QueryContext {
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
			gameMode = gameMode.name
		)
	}

	/**
	 * Returns the current time period, and seconds remaining until next period, e.g. "morning (123 seconds until noon)"
	 *
	 * ticks/second: 20
	 * 		0 ticks (06:00) – sunrise start, dawn.
	 * 		1000 ticks (07:00) – daytime start, morning.
	 * 		6000 ticks (12:00) – noon, midday.
	 * 		12000 ticks (18:00) – sunset start, dusk.
	 * 		13000 ticks (19:00) – night start.
	 * 		18000 ticks (00:00) – midnight.
	 * 		24000 ticks (06:00) – next sunrise, day cycle repeats.
	 */
	private fun getTimeString(): String {
		/*

		 */
		val world = server.worlds.firstOrNull() ?: return "unknown time"
		val dayTime = world.time
		val (period, nextPeriodTime) = when {
			dayTime in 0 until 1000 -> "dawn" to 1000
			dayTime in 1000 until 6000 -> "morning" to 6000
			dayTime in 6000 until 12000 -> "noon" to 12000
			dayTime in 12000 until 13000 -> "dusk" to 13000
			dayTime in 13000 until 18000 -> "night" to 18000
			dayTime in 18000 until 24000 -> "midnight" to 24000
			else -> "unknown" to 0
		}
		val nextPeriod = mapOf(
			"dawn" to "morning",
			"morning" to "noon",
			"noon" to "dusk",
			"dusk" to "night",
			"night" to "midnight",
			"midnight" to "dawn",
		)

		val ticksUntilNext = if(nextPeriodTime >= dayTime) {
			nextPeriodTime - dayTime
		} else {
			24000 - dayTime + nextPeriodTime
		}
		val secondsUntilNext = ticksUntilNext / 20
		return "$period ($secondsUntilNext seconds until ${nextPeriod[period]})"
	}
}