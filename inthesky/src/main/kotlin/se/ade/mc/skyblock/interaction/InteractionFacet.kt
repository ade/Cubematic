package se.ade.mc.skyblock.interaction

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin

class InteractionFacet(val plugin: CubematicSkyPlugin): Aspect(plugin) {
	override fun enable() {
		plugin.server.pluginManager.registerEvents(listener, plugin)
	}

	override fun disable() {
		TODO("Not yet implemented")
	}

	private val listener = object : Listener {
		@EventHandler
		fun on(e: PlayerInteractEvent) {
			boneMealOnDirtInteraction(e)
		}
	}
}