package se.ade.mc.skyblock.generator.nether

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo
import kotlin.math.abs
import kotlin.random.Random

// TODO Configuration opportunity
private val supportedNetherBiomes = listOf(
	Biome.NETHER_WASTES,
	Biome.SOUL_SAND_VALLEY,
	Biome.CRIMSON_FOREST,
	Biome.WARPED_FOREST,
	Biome.BASALT_DELTAS
)

class NetherBiomeProvider(
	private val worldSpawnX: Int,
	private val worldSpawnZ: Int,
	private val supportedBiomes: List<Biome>,
) : BiomeProvider() {
	// TODO Configuration support for these

	/** Grid size in chunks (X×X chunks) */
	private val biomeGridSize = 3

	/** Chunk radius to consider as spawn area */
	private val spawnChunkRadius = 2

	/** Whether to use a consistent biome at spawn area. */
	private val useConsistentSpawnBiome = true

	/** Default spawn area biome for nether. */
	private val spawnBiome = Biome.NETHER_WASTES  // Default spawn biome for nether

	override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int): Biome {
		// Check if we should use a consistent biome at spawn
		if (useConsistentSpawnBiome) {
			val chunkX = x shr 4
			val chunkZ = z shr 4
			val spawnChunkX = worldSpawnX shr 4
			val spawnChunkZ = worldSpawnZ shr 4

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