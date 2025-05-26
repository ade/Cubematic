package se.ade.mc.skyblock.nether

import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.Material
import org.bukkit.World
import kotlin.random.Random

/**
 * When a nether portal is created (in the nether), this rule generates a netherrack outline
 * around the base of the portal, then replaces 2-4 random blocks with either
 * warped nylium or crimson nylium (one type per portal), without replacing portal blocks themselves.
 */
fun portalGeneratesNetherGrassRule(e: PortalCreateEvent) {
    val world = e.world
    if (world.environment != World.Environment.NETHER) return

    val portalBlocks = e.blocks.map { it.block }
    if (portalBlocks.isEmpty()) return

    // Find the base Y level of the portal (lowest Y among portal blocks)
    val baseY = portalBlocks.minOf { it.y }

    // Get the outline (perimeter) of the portal blocks at the base Y level
    val basePortalBlocks = portalBlocks.filter { it.y == baseY }
    val outlinePositions = mutableSetOf<Pair<Int, Int>>()
    val directions = listOf(
        Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
        Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
    )
    for (block in basePortalBlocks) {
        for ((dx, dz) in directions) {
            val nx = block.x + dx
            val nz = block.z + dz
            // Only add if not part of the portal base
            if (basePortalBlocks.none { it.x == nx && it.z == nz }) {
                outlinePositions.add(Pair(nx, nz))
            }
        }
    }

    // Choose nylium type for this portal
    val nyliumType = if (Random.nextBoolean()) Material.WARPED_NYLIUM else Material.CRIMSON_NYLIUM
    // Choose how many nylium blocks to place (2-4, but not more than outline size)
    val nyliumCount = minOf(Random.nextInt(2, 5), outlinePositions.size)

    // Place netherrack at all outline positions (at baseY-1)
    val outlineBlocks = outlinePositions.map { (x, z) -> world.getBlockAt(x, baseY - 1, z) }
    outlineBlocks.forEach { it.type = Material.NETHERRACK }

    // Replace nyliumCount of them with nylium
    outlineBlocks.shuffled().take(nyliumCount).forEach { it.type = nyliumType }
}
