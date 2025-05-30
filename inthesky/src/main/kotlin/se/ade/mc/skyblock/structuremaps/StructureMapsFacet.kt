package se.ade.mc.skyblock.structuremaps

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.MapInitializeEvent
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin

class StructureMapsFacet(private val plugin: CubematicSkyPlugin): Aspect(plugin) {
	val listener = object: Listener {
		@EventHandler
		fun on(e: MapInitializeEvent) {
			reloadStructureMapData(e, plugin)
		}
	}

	override fun enable() {
		plugin.server.pluginManager.registerEvents(listener, plugin)
	}

	override fun disable() {

	}
}