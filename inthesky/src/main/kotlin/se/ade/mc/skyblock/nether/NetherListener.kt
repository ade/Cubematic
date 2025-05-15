package se.ade.mc.skyblock.nether

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.MagmaCube
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PiglinBarterEvent
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.random.Random

class NetherListener(private val plugin: CubematicSkyPlugin) : Listener {
	@EventHandler
	fun onMobDeath(event: EntityDeathEvent) {
		mobsDropNetherrackRule(event)
	}

	@EventHandler
	fun onEvent(event: PreCreatureSpawnEvent) {
		// Currently disabled, using default structure generation
		// witherCanSpawnOnWitherRoseOutsideFortressRule(event)
	}

	@EventHandler
	fun onEvent(event: CreatureSpawnEvent) {
		// Currently disabled, using default structure generation
	    // fortressMobsSpawnOnNetherBricksRule(event)
	}

	@EventHandler
	fun onEvent(event: PiglinBarterEvent) {
		barterSchematicRule(event, plugin)
	}
}