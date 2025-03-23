package se.ade.mc.skyblock.generator.overworld

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.generator.overworld.biomes.AdeBiomeProvider
import se.ade.mc.skyblock.generator.overworld.biomes.supportedBiomes
import java.util.*

class OverworldGenerator(private val plugin: CubematicSkyPlugin) : ChunkGenerator() {
	private val worldSpawnX = 0
	private val worldSpawnZ = 0
	private val islandY = 64

	override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		// Check if this is the spawn chunk
		if (chunkX == 0 && chunkZ == 0) {
			createSkyblockIsland(chunkData)
		}
	}

	private fun createSkyblockIsland(chunkData: ChunkData) {
		// L-shaped island made of exactly 81 dirt blocks (three 3x3x3 units)
		// First 3x3x3 segment (center)
		for (x in 7..9) {
			for (z in 7..9) {
				for (y in islandY-2..islandY-1) {
					chunkData.setBlock(x, y, z, Material.DIRT)
				}
				// Top layer is grass
				chunkData.setBlock(x, islandY, z, Material.GRASS_BLOCK)
			}
		}

		// Second 3x3x3 segment (extending in +X direction)
		for (x in 10..12) {
			for (z in 7..9) {
				for (y in islandY-2..islandY-1) {
					chunkData.setBlock(x, y, z, Material.DIRT)
				}
				chunkData.setBlock(x, islandY, z, Material.GRASS_BLOCK)
			}
		}

		// Third 3x3x3 segment (extending in +Z direction from center)
		for (x in 7..9) {
			for (z in 10..12) {
				for (y in islandY-2..islandY-1) {
					chunkData.setBlock(x, y, z, Material.DIRT)
				}
				chunkData.setBlock(x, islandY, z, Material.GRASS_BLOCK)
			}
		}
	}

	override fun getDefaultPopulators(world: World): List<BlockPopulator?> {
		return listOf(TreePopulator(plugin))
	}

	override fun getFixedSpawnLocation(world: World, random: Random): Location {
		return Location(world, 8.5, islandY + 1.0, 8.5)
	}

	override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
		return AdeBiomeProvider(worldSpawnX, worldSpawnZ, supportedBiomes)
	}
}