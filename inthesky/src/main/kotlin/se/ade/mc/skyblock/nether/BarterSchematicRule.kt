package se.ade.mc.skyblock.nether

import org.bukkit.World
import org.bukkit.event.entity.PiglinBarterEvent
import org.bukkit.generator.structure.Structure
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.structuremaps.createStructureMap
import kotlin.random.Random

fun barterSchematicRule(e: PiglinBarterEvent, plugin: CubematicSkyPlugin) {
	if (e.entity.world.environment != World.Environment.NETHER) {
		return
	}

	if(Random.nextInt(1, 100) == 1) {
		val item = createStructureMap(
			loc = e.entity.location,
			plugin = plugin,
			structure = Structure.FORTRESS,
			title = "Fortress",
		)
		e.outcome.add(item)
	}
}