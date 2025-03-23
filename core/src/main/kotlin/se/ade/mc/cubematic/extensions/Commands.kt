@file:Suppress("UnstableApiUsage")

package se.ade.mc.cubematic.extensions

import com.mojang.brigadier.Command
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

	fun command(name: String, builder: SubCommandBuilder.() -> Unit) {
		val subBuilder = SubCommandBuilder(name)
		subBuilder.builder()
		rootCommands.add(subBuilder.build())
	}

	internal fun buildAll() = rootCommands
}

class SubCommandBuilder(private val name: String) {
	private val node = Commands.literal(name)

	fun executes(action: (CommandContext<CommandSourceStack>) -> Unit) {
		node.executes {
			action(it)
			Command.SINGLE_SUCCESS
		}
	}

	fun playerExecutes(action: (CommandContext<CommandSourceStack>, Player) -> Unit) {
		node.executes { context ->
			val sender = context.source.sender
			if (sender is Player) {
				action(context, sender)
			} else {
				sender.sendMessage("This command can only be used by players.")
			}

			Command.SINGLE_SUCCESS
		}
	}

	fun subcommand(name: String, builder: SubCommandBuilder.() -> Unit) {
		val subBuilder = SubCommandBuilder(name)
		subBuilder.builder()
		node.then(subBuilder.build())
	}

	internal fun build(): LiteralCommandNode<CommandSourceStack> = node.build()
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