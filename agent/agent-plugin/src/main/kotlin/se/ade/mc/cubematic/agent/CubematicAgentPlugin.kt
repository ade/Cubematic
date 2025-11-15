package se.ade.mc.cubematic.agent

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.agent.main.CubeAgentConvo
import se.ade.mc.cubematic.agent.main.QueryContext
import se.ade.mc.cubematic.agent.main.ServerInfo
import se.ade.mc.cubematic.extensions.commands

class CubematicAgentPlugin: JavaPlugin() {
	override fun onEnable() {
		logger.info("Cubematic Agent Plugin enabled")
		server.pluginManager.registerEvents(eventHandler, this)

		commands {
			command("ca") {
				subcommand("ask") {
					playerExecGreedyString("question") { ctx, s, player ->
						player.sendMessage("You said: $s")
					}
				}
			}
		}
	}

	override fun onDisable() {
		logger.info("Cubematic Agent Plugin disabled")
	}

	private val eventHandler = object: Listener {
		@EventHandler
		fun onEvent(e: PlayerChatEvent) {
			logger.info("Chat event: ${e.message}")
			val context = e.player.queryContext()
			logger.info("Context: $context")

			GlobalScope.launch {
				val r = CubeAgentConvo().query(e.message, context) {
					// Lambda (partial text response) not used for now
				}
				logger.info("Agent response: $r")
				e.player.sendMessage(r)
			}
		}
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