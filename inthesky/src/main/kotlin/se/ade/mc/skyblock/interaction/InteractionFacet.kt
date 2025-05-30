package se.ade.mc.skyblock.interaction

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.mobs.witchDropsCauldronRule
import se.ade.mc.skyblock.interaction.PushBlockInLavaRule

class InteractionFacet(val plugin: CubematicSkyPlugin): Aspect(plugin) {
	override fun enable() {
		plugin.server.pluginManager.registerEvents(listener, plugin)
		charcoalOnCampfireRecipe(plugin)
		cutGlassRule(plugin)
		addListener(PushBlockInLavaRule)
	}

	override fun disable() {

	}

	private val listener = object : Listener {
		@EventHandler
		fun on(e: PlayerInteractEvent) {
			shortGrassOnDirtInteraction(e)
		}

		@EventHandler
		fun on(e: BlockBurnEvent) {
			charcoalLogBurnEvent(e)
		}
	}
}