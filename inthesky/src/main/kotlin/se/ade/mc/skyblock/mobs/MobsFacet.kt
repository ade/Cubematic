package se.ade.mc.skyblock.mobs

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin

class MobsFacet(private val plugin: CubematicSkyPlugin): Aspect(plugin) {
	private val eventListener = object: Listener {
		@EventHandler
		fun onEvent(e: CreatureSpawnEvent) {
			witchSpawnBoostRule(e, plugin)
		}

		@EventHandler
		fun onEvent(e: EntityDeathEvent) {
			witchDropsCauldronRule(e)
		}
	}

	override fun enable() {
		plugin.server.pluginManager.registerEvents(eventListener, plugin)
	}

	override fun disable() {

	}
}