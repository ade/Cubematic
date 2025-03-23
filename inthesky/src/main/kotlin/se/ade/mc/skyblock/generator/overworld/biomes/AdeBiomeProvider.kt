package se.ade.mc.skyblock.generator.overworld.biomes

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo
import kotlin.math.abs
import kotlin.random.Random

class AdeBiomeProvider(
    private val worldSpawnX: Int,
    private val worldSpawnZ: Int,
    private val supportedBiomes: List<Biome>,
) : BiomeProvider() {
    // Grid size in chunks (X×X chunks)
    // TODO configuration support for this
    private val biomeGridSize = 3

    // Configuration options
    private val useConsistentSpawnBiome = true  // Whether to use a consistent biome at spawn
    private val spawnBiome = Biome.PLAINS       // The biome to use at spawn
    private val spawnChunkRadius = 2            // Radius to consider as spawn area

    override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
        // Check if we should use a consistent biome at spawn
        if (useConsistentSpawnBiome) {
            // Convert block coordinates to chunk coordinates
            val chunkX = x shr 4
            val chunkZ = z shr 4
            val spawnChunkX = worldSpawnX shr 4
            val spawnChunkZ = worldSpawnZ shr 4

            // Check if the current position is within spawn chunks
            if (abs(chunkX - spawnChunkX) <= spawnChunkRadius &&
                abs(chunkZ - spawnChunkZ) <= spawnChunkRadius) {
                return spawnBiome
            }
        }

        // Outside spawn chunks or consistent spawn biome disabled, use the biome grid system
        val gridX = Math.floorDiv(x, 16 * biomeGridSize)
        val gridZ = Math.floorDiv(z, 16 * biomeGridSize)

        // Use the world seed combined with grid coordinates for consistent generation
        val random = Random(worldInfo.seed + gridX.toLong() * 341873128712L + gridZ.toLong() * 132897987541L)

        return supportedBiomes.random(random)
    }

    override fun getBiomes(worldInfo: WorldInfo): List<Biome> {
        return supportedBiomes
    }
}