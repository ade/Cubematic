package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.random.Random

class InteractionFacet(val plugin: CubematicSkyPlugin): Aspect(plugin) {
	override fun enable() {
		plugin.server.pluginManager.registerEvents(listener, plugin)
		charcoalOnCampfireRecipe(plugin)
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

		@EventHandler
		fun on(e: EntityDeathEvent) {
			witchDropsCauldronRule(e)
			creeperDropsBlazePowderRule(e)
		}
	}
}