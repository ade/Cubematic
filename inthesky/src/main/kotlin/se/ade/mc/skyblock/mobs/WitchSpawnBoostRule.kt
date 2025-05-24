package se.ade.mc.skyblock.mobs

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.generator.structure.Structure
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import se.ade.mc.cubematic.extensions.containsInclusive
import se.ade.mc.cubematic.extensions.hostileMobSpawnable
import se.ade.mc.skyblock.CubematicSkyPlugin
import kotlin.random.Random

private const val EXTRA_WITCHES_MIN = 1
private const val EXTRA_WITCHES_MAX = 4

/**
 * Witch Hut / Swamp Hut witch spawning boost rule.
 * Makes witches spawn faster if constructing a witch farm.
 */
fun witchSpawnBoostRule(e: CreatureSpawnEvent, plugin: CubematicSkyPlugin) {
    if(e.entity.type != EntityType.WITCH)
        return

    val debug = plugin.config.debug
    val loc = e.location
    val world = loc.world ?: return

    // Make sure we aren't adding extra witches to our custom extra spawn event!
    if(e.spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM)
        return

    val struct = world.getStructures(loc.chunk.x, loc.chunk.z, Structure.SWAMP_HUT).firstOrNull {
        // Structure mob spawn box is inclusive! For example, a structure with bbox
        // [minX=1232.0, minY=64.0, minZ=576.0, maxX=1240.0, maxY=70.0, maxZ=582.0]
        // can spawn a witch at [x=1239.5,y=67.0,z=582.5], even though the bounding box
        // technically only cover the area in Z from 576 to <582.
        // The above are actual coordinates from a test world with a Swamp Hut in 1.21.5 (papermc)
        // So - the normal BoundingBox.contains() method does not work here, since it is exclusive.
        it.boundingBox.containsInclusive(loc)
    } ?: return // If null - Normal witch spawn, not in a Swamp Hut.


    // Scan bounding box for all valid spawn locations
    // As noted above, the bounding box is inclusive, so we use the max values as well
    val bbox: BoundingBox = struct.boundingBox
    val validSpawnLocations = mutableListOf<Vector>()
    for (x in bbox.minX.toInt() .. bbox.maxX.toInt()) {
        for (y in bbox.minY.toInt().. bbox.maxY.toInt()) {
            for (z in bbox.minZ.toInt() .. bbox.maxZ.toInt()) {
                val block = world.getBlockAt(x, y, z)
                val blockAbove = block.getRelative(BlockFace.UP)
                val hasWitch = world.getNearbyEntities(BoundingBox(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0))
                    .any { it.type == EntityType.WITCH }

                if (!hasWitch && block.hostileMobSpawnable() && blockAbove.type == Material.AIR) {
                    validSpawnLocations.add(Vector(x,y,z))
                }
            }
        }
    }

    if (debug && validSpawnLocations.isEmpty()) {
        plugin.logger.warning { "No valid spawn locations for extra witches in Swamp Hut at $loc" }
        return
    }

    val extraWitches = Random.nextInt(EXTRA_WITCHES_MIN, EXTRA_WITCHES_MAX + 1)
    if(debug) {
        plugin.logger.info { "Spawning $extraWitches extra witches..." }
    }

    validSpawnLocations
        .sortedBy { Random.nextDouble() }
        .take(extraWitches)
        .forEach { vector ->
            // It appears that vanilla spawns witches at the center of the block,
            // so we add a small offset to the vector to match that behavior.
            val xOffset = 0.5
            val zOffset = 0.5

            val spawnLocation = Location(world, vector.x + xOffset, vector.y, vector.z + zOffset)
            world.spawnEntity(spawnLocation, EntityType.WITCH, CreatureSpawnEvent.SpawnReason.CUSTOM)

            if(debug) {
                plugin.logger.info("Spawned extra witch at $spawnLocation!")
            }
    }
}