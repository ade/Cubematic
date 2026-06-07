package se.ade.cubematic.agent

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import org.slf4j.LoggerFactory
import se.ade.mc.cubematic.core.agent.main.CubeAgent
import se.ade.mc.cubematic.core.agent.main.CubeAgentConvo
import se.ade.mc.cubematic.core.agent.main.ProcessEvent
import se.ade.mc.cubematic.core.agent.main.QueryContext
import se.ade.mc.cubematic.core.agent.main.ServerInfo
import se.ade.mc.cubematic.core.agent.utils.WorldTimeFormat
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

private const val MAX_CHAT_HISTORY = 10

object CubematicAgent : ModInitializer {
	private val logger = LoggerFactory.getLogger("cubematic-agent")

	private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	private val config: AgentConfig by lazy { AgentConfig.load() }
	private val cubeAgent: CubeAgent by lazy {
		CubeAgent(config.inferenceConfig.providers.first().asInferenceProvider())
	}

	private var server: MinecraftServer? = null

	private var globalConvo: Pair<Instant, CubeAgentConvo>? = null
	private val convoMap = mutableMapOf<UUID, CubeAgentConvo>()
	private val chatHistory: MutableList<Pair<String, String>> = mutableListOf()

	override fun onInitialize() {
		logger.info("Cubematic Agent (Fabric) initializing")

		ServerLifecycleEvents.SERVER_STARTING.register { srv -> server = srv }
		ServerLifecycleEvents.SERVER_STOPPING.register { server = null }

		registerCommands()
		registerChatListener()
	}

	// region commands

	private fun registerCommands() {
		CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
			dispatcher.register(
				Commands.literal("qb")
					.then(
						Commands.literal("ask")
							.then(
								Commands.argument("question", StringArgumentType.greedyString())
									.executes { ctx -> onAsk(ctx) }
							)
					)
					.then(
						Commands.literal("re")
							.then(
								Commands.argument("response", StringArgumentType.greedyString())
									.executes { ctx -> onRespond(ctx) }
							)
					)
			)
		}
	}

	private fun onAsk(ctx: CommandContext<CommandSourceStack>): Int {
		val player = ctx.source.player ?: run {
			ctx.source.sendSystemMessage(Component.literal("This command can only be used by a player."))
			return 0
		}
		val question = StringArgumentType.getString(ctx, "question")
		val context = player.queryContext()

		scope.launch {
			val convo = cubeAgent.createConvo().also { convoMap[player.uuid] = it }

			val resp = convo.query(question, context) { event ->
				if (event is ProcessEvent.ProgressMessage)
					player.sendPlain(event.message)
			}.getOrElse {
				player.sendPlain("An error occurred: ${it.message}")
				return@launch
			}

			player.qbSend(resp)
		}

		player.sendPlain("Loading...")
		return 1
	}

	private fun onRespond(ctx: CommandContext<CommandSourceStack>): Int {
		val player = ctx.source.player ?: run {
			ctx.source.sendSystemMessage(Component.literal("This command can only be used by a player."))
			return 0
		}
		val response = StringArgumentType.getString(ctx, "response")
		val context = player.queryContext()

		scope.launch {
			convoMap.getOrPut(player.uuid) { cubeAgent.createConvo() }
				.query(response, context) {
					// Partial responses not used for now.
				}
				.getOrElse {
					player.sendPlain("An error occurred: ${it.message}")
					return@launch
				}
				.let { resp -> player.qbSend(resp) }
		}

		player.sendPlain("Loading...")
		return 1
	}

	// endregion

	// region chat

	private fun registerChatListener() {
		ServerMessageEvents.CHAT_MESSAGE.register { message, sender, _ ->
			val content = message.signedContent()

			if (content.lowercase().contains("@qb")) {
				handleChatMention(sender, content)
			}

			val senderName = sender.gameProfile.name
			while (chatHistory.size >= MAX_CHAT_HISTORY) {
				chatHistory.removeAt(0)
			}
			chatHistory.add(senderName to content)
		}
	}

	private fun handleChatMention(sender: ServerPlayer, message: String) {
		val srv = server ?: return
		val context = sender.queryContext(chatHistory.toList())

		scope.launch {
			val gc = globalConvo
			val convo = if (gc == null || Clock.System.now() - gc.first > 15.minutes) {
				cubeAgent.createConvo().also { globalConvo = Clock.System.now() to it }
			} else {
				gc.second
			}

			val resp = convo.query(message, context) { event ->
				if (event is ProcessEvent.ProgressMessage)
					srv.qbBroadcast("[${event.message}]")
			}.getOrElse {
				srv.broadcast(
					Component.literal("<QB> ")
						.append(Component.literal("An error occurred: ${it.message}").withStyle(ChatFormatting.RED))
				)
				return@launch
			}

			srv.qbBroadcast(resp)
		}
	}

	// endregion

	// region context

	private fun ServerPlayer.queryContext(chatHistory: List<Pair<String, String>> = emptyList()): QueryContext {
		val level = level()
		val box = boundingBox.inflate(16.0)

		val entities = level.getEntities(this, box) { it !is Player }
			.groupBy { entity ->
				val type = entity.type.toShortString()
				if (entity is ItemEntity) {
					"$type:${BuiltInRegistries.ITEM.getKey(entity.item.item)}"
				} else {
					type
				}
			}
			.map { (type, ents) ->
				if (ents.size == 1) type else "$type (x${ents.size})"
			}
			.take(16)

		val inventoryItems = inventory.nonEquipmentItems
			.filterNot { it.isEmpty }
			.groupBy { BuiltInRegistries.ITEM.getKey(it.item).toString() }
			.map { (key, stacks) ->
				QueryContext.InventoryItem(key, stacks.sumOf { it.count })
			}

		return QueryContext(
			serverInfo = ServerInfo(
				version = server?.serverVersion ?: "no server"
			),
			playerName = gameProfile.name,
			playerLevel = experienceLevel,
			health = health.toInt(),
			foodLevel = foodData.foodLevel,
			location = QueryContext.LocationContext(
				worldName = level.dimension().identifier().toShortString(),
				x = blockX,
				y = blockY,
				z = blockZ
			),
			time = timeString(),
			nearbyEntities = entities,
			inventoryItems = inventoryItems,
			gameMode = gameMode().getName(),
			chatHistory = chatHistory,
		)
	}

	private fun timeString(): String {
		val overworld = server?.overworld() ?: return "unknown time"
		val dayTime = overworld.overworldClockTime % 24000
		return WorldTimeFormat(dayTime).getTimeString()
	}

	// endregion

	// region messaging helpers

	private fun qbComponent(text: String): Component =
		Component.literal("<")
			.append(Component.literal("QB").withStyle(ChatFormatting.LIGHT_PURPLE))
			.append(Component.literal("> "))
			.append(Component.literal(text))

	private fun ServerPlayer.qbSend(text: String) {
		server?.execute { sendSystemMessage(qbComponent(text)) }
	}

	private fun ServerPlayer.sendPlain(text: String) {
		server?.execute { sendSystemMessage(Component.literal(text)) }
	}

	private fun MinecraftServer.qbBroadcast(text: String) {
		broadcast(qbComponent(text))
	}

	private fun MinecraftServer.broadcast(component: Component) {
		execute { playerList.broadcastSystemMessage(component, false) }
	}

	// endregion
}