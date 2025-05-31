package se.ade.mc.skyblock.generator.overworld

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.type.Leaves
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.util.Random
import kotlin.math.abs
import kotlin.math.min

class HardOverworldGenerator(
    private val plugin: CubematicSkyPlugin,
    private val originX: Int = 0,
    private val originY: Int = 64,
    private val originZ: Int = 0,
    private val trunkHeight: Int = 5,
    private val crownHeight: Int = 4,
    private val dirtBlocks: Int = 64
) : ChunkGenerator() {
    override fun generateSurface(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        chunkData: ChunkData
    ) {
        // No-op: island and tree generation moved to Populator for full region access
    }

    override fun shouldGenerateStructures(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int
    ): Boolean {
        return true
    }

    override fun getFixedSpawnLocation(world: World, random: Random): Location? {
        return Location(world, originX.toDouble()-1, originY.toDouble()+1, originZ.toDouble()-1)
    }

    override fun getDefaultPopulators(world: World): List<BlockPopulator?> {
        return listOf(Populator(
            originX = originX,
            originY = originY,
            originZ = originZ,
            trunkHeight = trunkHeight,
            crownHeight = crownHeight,
            dirtBlocks = dirtBlocks,
        ))
    }

    private class Populator(
        val originX: Int,
        val originY: Int,
        val originZ: Int,
        private val trunkHeight: Int,
        private val crownHeight: Int,
        private val dirtBlocks: Int
    ) : BlockPopulator() {
        override fun populate(
            worldInfo: WorldInfo,
            random: Random,
            chunkX: Int,
            chunkZ: Int,
            limitedRegion: LimitedRegion
        ) {
            // Only generate island and tree at origin in chunk 0,0
            if (chunkX == 0 && chunkZ == 0) {
                generateStartIslandAndTree(limitedRegion, originX, originY, originZ)
            }
        }

        private fun generateStartIslandAndTree(region: LimitedRegion, centerX: Int, y: Int, centerZ: Int) {
            val dirtBlocks = this.dirtBlocks
            val dirtPositions = mutableListOf<Pair<Int, Int>>()
            dirtPositions.add(Pair(centerX, centerZ))
            var layer = 1
            while (dirtPositions.size < dirtBlocks) {
                val candidates = mutableListOf<Pair<Int, Int>>()
                for (dx in -layer..layer) {
                    for (dz in -layer..layer) {
                        if (abs(dx) == layer || abs(dz) == layer) {
                            val pos = Pair(centerX + dx, centerZ + dz)
                            if (!dirtPositions.contains(pos)) {
                                candidates.add(pos)
                            }
                        }
                    }
                }
                candidates.sortBy { (x, z) -> (x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ) }
                for (pos in candidates) {
                    if (dirtPositions.size < dirtBlocks) {
                        dirtPositions.add(pos)
                    } else {
                        break
                    }
                }
                layer++
            }
            for ((x, z) in dirtPositions) {
                region.setType(x, y, z, Material.DIRT)
            }
            generateAcaciaTree(
                region = region,
                x = centerX,
                y = y + 1,
                z = centerZ,
                trunkHeight = trunkHeight,
                crownHeight = crownHeight
            )
        }

        private fun generateAcaciaTree(
            region: LimitedRegion,
            x: Int,
            y: Int,
            z: Int,
            trunkHeight: Int = 5,
            crownHeight: Int = 4
        ) {
            // 1. Pyramid-shaped crown (leaves first)
            for (level in 0 until crownHeight) {
                val crownY = y + (trunkHeight - crownHeight + 1) + level
                val radius = crownHeight - level - 1
                for (dx in -radius..radius) {
                    for (dz in -radius..radius) {
                        region.setType(x + dx, crownY, z + dz, Material.ACACIA_LEAVES)

                        // set leaves to non-decaying by specifying "distance" to log
                        region.getBlockData(x + dx, crownY, z + dz).let { blockData ->
                            if (blockData is Leaves) {
                                blockData.distance = crownHeight.coerceIn(blockData.minimumDistance, blockData.maximumDistance)
                                region.setBlockData(x + dx, crownY, z + dz, blockData)
                            }
                        }
                    }
                }
            }
            // 2. Trunk goes up inside the crown, but stops before the very top leaf block
            for (i in 0 until trunkHeight) {
                region.setType(x, y + i, z, Material.ACACIA_LOG)
            }
        }
    }
}
