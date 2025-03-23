package se.ade.mc.skyblock

import com.mojang.brigadier.Command
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

interface CommandHandler: DreamCommandHandler, MiscCommandHandler

interface MiscCommandHandler {

}

interface DreamCommandHandler {
	fun onCreateDreamWorldCommand()
	fun onEnterDreamWorldCommand(player: Player)
	fun onLeaveDreamWorldCommand(player: Player)
	fun onDestroyDreamWorldCommand()
	fun onStashInventoryCommand(player: Player)
	fun onPopInventoryCommand(player: Player)
}

class CommandRegistrar(private val plugin: JavaPlugin) {
	fun register(handler: CommandHandler) {
		@Suppress("UnstableApiUsage")
		val cmd: LiteralCommandNode<CommandSourceStack> = Commands.literal("dream")
			.then(
				Commands.literal("create")
					.executes { context ->
						handler.onCreateDreamWorldCommand()
						context.source.sender.sendMessage("Dream world created")
						return@executes Command.SINGLE_SUCCESS
					}
			)
			.then(
				Commands.literal("enter")
					.executes { context ->
						val player = context.source.sender as? Player
							?: run {
								context.source.sender.sendMessage("This command can only be used by players")
								return@executes Command.SINGLE_SUCCESS
							}

						handler.onEnterDreamWorldCommand(player)
						return@executes Command.SINGLE_SUCCESS;
					}
			)
			.then(
				Commands.literal("leave")
					.executes { context ->
						val player = context.source.sender as? Player
							?: run {
								context.source.sender.sendMessage("This command can only be used by players")
								return@executes Command.SINGLE_SUCCESS
							}

						handler.onLeaveDreamWorldCommand(player)
						return@executes Command.SINGLE_SUCCESS
					}
			)
			.then(
				Commands.literal("destroy")
					.executes { context ->
						handler.onDestroyDreamWorldCommand()
						context.source.sender.sendMessage("Dream world destroyed")
						return@executes Command.SINGLE_SUCCESS
					}
			)
			.then(
				Commands.literal("inventory")
					.then(
						Commands.literal("stash")
							.executes { context ->
								val player = context.source.sender as? Player
									?: run {
										context.source.sender.sendMessage("This command can only be used by players")
										return@executes Command.SINGLE_SUCCESS
									}

								handler.onStashInventoryCommand(player)
								return@executes Command.SINGLE_SUCCESS
							}
					)
					.then(Commands.literal("pop")
						.executes { context ->
							val player = context.source.sender as? Player
								?: run {
									context.source.sender.sendMessage("This command can only be used by players")
									return@executes Command.SINGLE_SUCCESS
								}

							handler.onPopInventoryCommand(player)
							return@executes Command.SINGLE_SUCCESS
						}
					)
			)
			.build()

		@Suppress("UnstableApiUsage")
		plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
			commands.registrar().register(cmd)
		}
	}
}