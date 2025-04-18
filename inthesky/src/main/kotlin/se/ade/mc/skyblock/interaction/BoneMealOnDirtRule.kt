package se.ade.mc.skyblock.interaction

import kotlinx.coroutines.*
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import se.ade.mc.cubematic.extensions.spend
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.measureTimedValue

/**
 * Allows the player to turn DIRT blocks into GRASS_BLOCK blocks with bone meal.
 * This is done by right-clicking the DIRT block with a BONE_MEAL item.
 * @return true if the event was consumed, false otherwise
 */
fun boneMealOnDirtInteraction(e: PlayerInteractEvent): Boolean {
	val action = e.action

	if(action != Action.RIGHT_CLICK_BLOCK) return false
	val clickedBlock = e.clickedBlock ?: return false
	if(clickedBlock.type != Material.DIRT) return false
	val item = e.item ?: return false
	if(item.type != Material.BONE_MEAL) return false
	val hand = e.hand ?: return false

	/*
	if(hasGrassSync(e.player))
		return false

	 */

	e.isCancelled = true
	clickedBlock.type = Material.GRASS_BLOCK
	boneMealGrowEffect(clickedBlock)

	if(e.player.gameMode == GameMode.CREATIVE) {
		return true
	} else {
		e.player.inventory.setItem(hand, e.player.inventory.getItem(hand).spend(1))
		return true
	}
}

/** Bone meal / Grow effect */
private fun boneMealGrowEffect(block: Block) {
	block.world.spawnParticle(
		/* particle */ Particle.HAPPY_VILLAGER,
		/* location */ block.location.add(0.5, 1.5, 0.5),
		/* count */ 10,
		/* offsetX */ 0.5,
		/* offsetY */ 0.5,
		/* offsetZ */ 0.5,
		/* extra (speed) */ 0.0,
		/* data */ null
	)
}

fun hasGrassSync(player: Player): Boolean {
	val timed = measureTimedValue {
		runBlocking {
			withTimeoutOrNull(3000) {
				withContext(Dispatchers.Default) {
					hasGrassInActiveChunksAsync(player.world, player.location)
				}
			} ?: run {
				player.sendMessage("Grass check timed out")
				return@runBlocking true
			}
		}
	}
	if(timed.value) {
		player.sendMessage("Grass found in active chunks in ${timed.duration.inWholeMilliseconds}ms")
	} else {
		player.sendMessage("No grass found in active chunks in ${timed.duration.inWholeMilliseconds}ms")
	}

	return timed.value
}

suspend fun hasGrassInActiveChunksAsync(world: World, startLocation: Location): Boolean {
	val maxHeight = world.maxHeight
	val minHeight = world.minHeight
	val sought = Material.GRASS_BLOCK

	// Sort chunks by distance to the starting location
	val sortedChunks = world.loadedChunks.sortedBy { chunk ->
		val chunkCenterX = (chunk.x * 16) + 8
		val chunkCenterZ = (chunk.z * 16) + 8
		val dx = chunkCenterX - startLocation.blockX
		val dz = chunkCenterZ - startLocation.blockZ
		sqrt((dx * dx + dz * dz).toDouble())
	}

	return coroutineScope {
		val deferredResults = sortedChunks.map { chunk ->
			async {
				var yOffset = 0
				val yRange = minHeight until maxHeight

				do {
					val yUp = startLocation.blockY + yOffset
					val yDown = startLocation.blockY - yOffset

					val searchUp = yUp in yRange
					val searchDown = yDown in yRange

					for (x in 0..15) {
						for (z in 0..15) {
							if (searchUp && chunk.getBlock(x, yUp, z).type == sought) {
								return@async true
							}
							if (yOffset != 0 && searchDown && chunk.getBlock(x, yDown, z).type == sought) {
								return@async true
							}
						}
					}

					yOffset++
				} while (searchUp || searchDown)

				// Not found
				false
			}
		}

		while (deferredResults.any { it.isActive }) {
			if (deferredResults.find { !it.isActive }?.await() == true) {
				deferredResults.forEach { it.cancel() }
				return@coroutineScope true
			}
			delay(10.microseconds)
		}
		deferredResults.any { it.await() }
	}
}