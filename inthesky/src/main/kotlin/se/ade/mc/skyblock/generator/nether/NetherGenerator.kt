package se.ade.mc.skyblock.generator.nether

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import se.ade.mc.skyblock.CubeInTheSkyPlugin
import java.util.*

// TODO Configuration opportunity
private val supportedNetherBiomes = listOf(
	Biome.NETHER_WASTES,
	Biome.SOUL_SAND_VALLEY,
	Biome.CRIMSON_FOREST,
	Biome.WARPED_FOREST,
	Biome.BASALT_DELTAS
)

class NetherGenerator(private val plugin: CubeInTheSkyPlugin) : ChunkGenerator() {
	// TODO Configuration opportunity
	private val islandY = 64

	override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		if (chunkX == 0 && chunkZ == 0) {
			createNetherIsland(chunkData, random)
		}
	}

	private fun createNetherIsland(chunkData: ChunkData, random: Random) {
		// Center position in the chunk - expanded to 7x7
		val startX = 5
		val endX = 11
		val startZ = 5
		val endZ = 11

		// Create a 7x7x5 netherrack island
		for (x in startX..endX) {
			for (z in startZ..endZ) {
				// Create 5-block deep platform
				for (y in (islandY-4)..islandY) {
					chunkData.setBlock(x, y, z, Material.NETHERRACK)
				}
			}
		}

		// Apply nylium only to the inner 5x5 area (keeping netherrack border)
		val innerStartX = 6
		val innerEndX = 10
		val innerStartZ = 6
		val innerEndZ = 10

		// Split the island diagonally - crimson nylium on top-right, warped on bottom-left
		for (x in innerStartX..innerEndX) {
			for (z in innerStartZ..innerEndZ) {
				if (x - innerStartX + z - innerStartZ <= 4) {
					// Bottom-left half (including diagonal)
					chunkData.setBlock(x, islandY, z, Material.WARPED_NYLIUM)
				} else {
					// Top-right half
					chunkData.setBlock(x, islandY, z, Material.CRIMSON_NYLIUM)
				}
			}
		}
	}

	override fun getDefaultPopulators(world: World): List<BlockPopulator?> {
		return listOf(NetherPopulator(plugin))
	}

	override fun getFixedSpawnLocation(world: World, random: Random): Location {
		return Location(world, 8.5, islandY + 1.0, 8.5)
	}

	override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
		// TODO Configuration opportunity (use custom or not)
		return NetherBiomeProvider(0, 0, supportedNetherBiomes)
	}

	override fun shouldGenerateStructures(): Boolean {
		// This will generate the metadata fortress "structure" without the blocks
		return false
	}

	override fun shouldGenerateDecorations(): Boolean {
		// This will generate nether fortresses and more
		return false
	}
}