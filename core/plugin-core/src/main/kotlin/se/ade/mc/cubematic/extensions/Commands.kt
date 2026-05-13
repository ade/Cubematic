@file:Suppress("UnstableApiUsage")

package se.ade.mc.cubematic.extensions

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * DSL for building commands with a fluent Kotlin syntax
 */
class CommandBuilder {
	private val rootCommands = mutableListOf<LiteralCommandNode<CommandSourceStack>>()

	fun command(name: String, builder: CommandNode.() -> Unit) {
		val commandNode = CommandNode(name)
		commandNode.builder()
		rootCommands.add(commandNode.build())
	}

	internal fun buildAll() = rootCommands
}

class CommandNode(private val name: String) {
	private val node = Commands.literal(name)

	fun command(name: String, builder: CommandNode.() -> Unit) {
		val subCommand = CommandNode(name)
		subCommand.builder()
		node.then(subCommand.build())
	}

	fun executes(action: (CommandContext<CommandSourceStack>) -> Unit) {
		node.executes {
			action(it)
			Command.SINGLE_SUCCESS
		}
	}

	fun withPlayer(builder: PlayerOnlyScope.() -> Unit) {
		val scope = PlayerOnlyScope(node)
		scope.builder()
	}

	internal fun build(): LiteralCommandNode<CommandSourceStack> = node.build()
}

class PlayerOnlyScope(private val parentNode: LiteralArgumentBuilder<CommandSourceStack>) {

	fun command(name: String, builder: PlayerOnlyScope.() -> Unit) {
		val literalNode = Commands.literal(name)
		val subScope = PlayerOnlyScope(literalNode)
		subScope.builder()
		parentNode.then(literalNode)
	}

	fun executes(action: (CommandContext<CommandSourceStack>, Player) -> Unit) {
		parentNode.executesWithPlayer { context, player ->
			action(context, player)
			Command.SINGLE_SUCCESS
		}
	}

	/**
	 * Adds a greedy string argument to the command and executes the given action with the argument and player.
	 */
	fun greedyString(
		paramName: String = "argument",
		action: (context: CommandContext<CommandSourceStack>, arg: String, player: Player) -> Unit
	) {
		parentNode.then(
			RequiredArgumentBuilder.argument<CommandSourceStack, String>(paramName, StringArgumentType.greedyString())
				.executesWithPlayer { context, player ->
					val arg: String = context.getArgument(paramName, String::class.java)
					action(context, arg, player)
					Command.SINGLE_SUCCESS
				}
		)
	}

	private fun <T:ArgumentBuilder<CommandSourceStack, T>> ArgumentBuilder<CommandSourceStack,T>.executesWithPlayer(action: (context: CommandContext<CommandSourceStack>, player: Player) -> Int): T {
		return this.executes { context ->
			val sender = context.source.sender
			if (sender is Player) {
				action(context, sender)
			} else {
				sender.sendMessage("This command can only be used by players.")
				0
			}
		}
	}
}

fun JavaPlugin.commands(builder: CommandBuilder.() -> Unit) {
	val commandBuilder = CommandBuilder()
	commandBuilder.builder()

	@Suppress("UnstableApiUsage")
	lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { commands ->
		commandBuilder.buildAll().forEach { node ->
			commands.registrar().register(node)
		}
	}
}