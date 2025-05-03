package se.ade.mc.skyblock.trader

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.WanderingTrader
import org.bukkit.event.world.TimeSkipEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

fun spawnTraderRule(e: TimeSkipEvent, plugin: JavaPlugin) {
	// newTime will normally be 0 here == morning.
	val newTime = (e.world.time + e.skipAmount) % 24000L

	if(e.skipReason == TimeSkipEvent.SkipReason.NIGHT_SKIP && e.skipAmount > 100L) {
		if(hasTraderAlready(e.world)) {
			// Trader already spawned, do nothing
			return
		}

		// Get the player with the highest level in the world
		val playerWithHighestLevel = e.world.players.maxByOrNull { it.level + it.exp }
			?: return // No players in the world, do nothing

		// Spawn chance is based on the player's level
		val spawnChance = playerWithHighestLevel.level

		// 1 level = 1 percentage point, all the way up to 100%
		if(Random.nextInt(0, 101) > spawnChance) {
			// Roll check failed.
			return
		}

		// Figure out a location to spawn the trader.
		val location = findSpawnLocation(playerWithHighestLevel.location)
			?: return // No valid spawn location found, do nothing

		val world = e.world

		if(newTime < 12000L) { // From morning to sunset
			val ent = world.spawnEntity(location, EntityType.WANDERING_TRADER) as WanderingTrader

			// Custom spawned trader doesn't despawn naturally unless we configure the delay to the default natural spawn value.
			ent.despawnDelay = 48_000
		}
	}
}

fun findSpawnLocation(location: Location): Location? {
	val world = location.world
	val radius = 16
	val centerX = location.blockX
	val centerZ = location.blockZ

	val validLocations = mutableListOf<Location>()

	for (xOffset in -radius..radius) {
		for (zOffset in -radius..radius) {
			val x = centerX + xOffset
			val z = centerZ + zOffset
			val highestY = world.getHighestBlockYAt(x, z)
			val spawnLocation = Location(world, x.toDouble(), highestY.toDouble() + 1, z.toDouble())

			// Check if the block below is solid and there's enough space above
			if (world.getBlockAt(x, highestY, z).type.isSolid &&
				world.getBlockAt(x, highestY + 1, z).type.isAir &&
				world.getBlockAt(x, highestY + 2, z).type.isAir
			) {
				validLocations.add(spawnLocation)
			}
		}
	}

	return validLocations.randomOrNull()
}

private fun hasTraderAlready(world: World): Boolean {
	// Check if a trader has already been spawned in the world
	return world.getEntitiesByClass(WanderingTrader::class.java)
		.any { it.isValid }
}