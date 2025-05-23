package se.ade.mc.skyblock.mobs

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.generator.structure.Structure
import org.bukkit.util.BoundingBox
import se.ade.mc.cubematic.extensions.hostileMobSpawnable
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.random.Random

/**
 * Witch Hut / Swamp Hut witch spawning boost rule.
 * Makes witches spawn faster if constructing a witch farm.
 */
fun witchSpawnBoostRule(e: CreatureSpawnEvent, plugin: CubematicSkyPlugin) {
    val loc = e.location
    val world = loc.world ?: return

    if(e.entity.type != EntityType.WITCH || e.spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM)
        return

    plugin.logger.info { "Witch CreatureSpawnEvent! $e" }

    // Locate nearest Swamp Hut (within 16 blocks, since we are already inside it)
    val result = world.locateNearestStructure(loc, Structure.SWAMP_HUT, 16, false)
        ?: run {
            plugin.logger.warning { "Structure 'SWAMP_HUT' not found in world: ${world.name} searching from $loc" }
            return
        }

    val chunkX = result.location.chunk.x
    val chunkZ = result.location.chunk.z
    val struct = world.getStructures(chunkX, chunkZ).firstOrNull {
        it.structure == Structure.SWAMP_HUT
    } ?: run {
        plugin.logger.warning { "Structure 'SWAMP_HUT' not found at: ${result.location}" }
        return
    }

    val bbox: BoundingBox = struct.boundingBox

    // Check if spawn location is inside the Swamp Hut bounding box
    if (!bbox.contains(loc.x, loc.y, loc.z)) run {
        plugin.logger.warning { "Spawn location $loc is outside the bounding box of the Swamp Hut!" }
        return
    }

    // Scan bounding box for all valid spawn locations
    val validSpawnLocations = mutableListOf<Location>()
    for (x in bbox.minX.toInt() until bbox.maxX.toInt()) {
        for (y in bbox.minY.toInt()until bbox.maxY.toInt()) {
            for (z in bbox.minZ.toInt() until bbox.maxZ.toInt()) {
                val baseLoc = Location(world, x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5)
                val block = world.getBlockAt(baseLoc)
                val blockAbove = block.getRelative(BlockFace.UP)

                if (block.hostileMobSpawnable() && blockAbove.type == Material.AIR) {
                    validSpawnLocations.add(baseLoc)
                }
            }
        }
    }

    if (validSpawnLocations.isEmpty()) {
        plugin.logger.info { "No valid spawn locations for extra witches in Swamp Hut at $loc" }
        return
    }

    // Roll dice for 1-3 extra witches
    val extraWitches = Random.nextInt(1, 4)
    plugin.logger.info { "Spawning $extraWitches extra witches..." }

    repeat(extraWitches) {
        val spawnLoc = validSpawnLocations.random()
        world.spawnEntity(spawnLoc, EntityType.WITCH, CreatureSpawnEvent.SpawnReason.CUSTOM)
        plugin.logger.info("Spawned extra witch at $spawnLoc!")
    }
}

