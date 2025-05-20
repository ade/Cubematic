package se.ade.mc.skyblock.nether

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.entity.PiglinBarterEvent
import org.bukkit.generator.structure.Structure
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.structuremaps.createStructureMap
import kotlin.random.Random

private const val BARTER_CHANCE_PERCENT = 1.0

/**
 * BarterSchematicRule adds a chance to get a nether fortress map when bartering with piglins.
 * The item is dropped in addition to the normal drop regardless of the outcome of the barter.
 */
fun barterSchematicRule(e: PiglinBarterEvent, plugin: CubematicSkyPlugin) {
	if (e.entity.world.environment != World.Environment.NETHER) {
		return
	}

	if(Random.nextDouble(0.0, 100.0) <= BARTER_CHANCE_PERCENT) {
		val item = createNetherFortressMap(e.entity.location, plugin)
		e.outcome.add(item)
	}
}

fun createNetherFortressMap(location: Location, plugin: CubematicSkyPlugin)
	= createStructureMap(
		loc = location,
		plugin = plugin,
		structure = Structure.FORTRESS,
		title = "Fortress",
	)