package se.ade.mc.skyblock.nether

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.CreatureSpawnEvent
import kotlin.random.Random

@Deprecated("Not used since we are generating nether with vanilla structures now.")
fun fortressMobsSpawnOnNetherBricksRule(event: CreatureSpawnEvent) {
	val entity = event.entity
	val location = entity.location
	val world = location.world

	// Check if in nether dimension
	if (world.environment != World.Environment.NETHER) {
		return
	}

	// Check if we manually spawned the wither skeleton.
	if(entity.type == EntityType.WITHER_SKELETON)
		return

	// Only handle natural spawns
	if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) {
		return
	}

	// Get the block the entity is spawning on
	val blockBelow = location.clone().subtract(0.0, 1.0, 0.0).block

	// Define fortress-specific blocks
	val fortressBlocks = setOf(
		Material.NETHER_BRICKS,
		Material.NETHER_BRICK_FENCE,
		Material.NETHER_BRICK_STAIRS,
		Material.NETHER_BRICK_SLAB
	)

	// Check if the entity is spawning on a fortress block
	if (!fortressBlocks.contains(blockBelow.type)) {
		return
	}

	// Cancel the original spawn
	event.isCancelled = true

	// Determine which fortress mob to spawn
	val fortressMob = chooseFortressMob(location)

	// If no mob was selected (due to conditions), just return
	if (fortressMob == null) {
		return
	}

	// Spawn the fortress mob
	world.spawnEntity(location, fortressMob)
}

private fun chooseFortressMob(location: Location): EntityType? {
	// Check conditions
	val lightLevel = location.block.lightLevel

	// Weighted random selection of fortress mobs
	val random = Random.nextDouble()

	return when {
		random < 0.1 && lightLevel <= 11 -> {
			// 10% chance for Blaze (light level ≤ 11)
			EntityType.BLAZE
		}
		random < 0.35 && lightLevel <= 7 -> {
			// 25% chance for Wither Skeleton (light level ≤ 7)
			EntityType.WITHER_SKELETON
		}
		random < 0.45 && lightLevel <= 7 -> {
			// 10% chance for regular Skeleton (light level ≤ 7)
			EntityType.SKELETON
		}
		random < 0.75 && lightLevel <= 7 -> {
			// 30% chance for Zombified Piglin (light level ≤ 7)
			EntityType.ZOMBIFIED_PIGLIN
		}
		random < 0.85 -> {
			// 10% chance for Magma Cube (no light restriction)
			EntityType.MAGMA_CUBE
		}
		else -> {
			// 15% chance for no spawn to control population density
			null
		}
	}
}

/** Hack to fix wither being able to spawn on wither roses without being in fortress */
fun witherCanSpawnOnWitherRoseOutsideFortressRule(event: PreCreatureSpawnEvent) {
	// Only process in nether dimension
	if (event.spawnLocation.world.environment != World.Environment.NETHER) {
		return
	}

	// Only handle natural spawns
	if (event.reason != CreatureSpawnEvent.SpawnReason.NATURAL) {
		return
	}

	// Check for wither rose specifically
	if (event.spawnLocation.block.type == Material.WITHER_ROSE) {
		// Check for nearby wither skeletons to prevent overcrowding
		val nearbyEntities = event.spawnLocation.world.getNearbyEntities(
			event.spawnLocation,
			16.0,
			8.0,
			16.0
		) { it.type == EntityType.WITHER_SKELETON }

		// If there are already 3+ wither skeletons nearby, don't spawn more
		if (nearbyEntities.size >= 3) {
			return
		}

		// Cancel the original spawn
		event.isCancelled = true

		// Light level check for wither skeletons
		if (event.spawnLocation.block.lightLevel <= 7) {
			// Change type
			event.spawnLocation.world.spawnEntity(event.spawnLocation, EntityType.WITHER_SKELETON)
		}
	}
}