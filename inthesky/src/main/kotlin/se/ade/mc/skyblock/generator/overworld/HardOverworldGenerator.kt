package se.ade.mc.skyblock.generator.overworld

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.util.Random

class HardOverworldGenerator(private val plugin: CubematicSkyPlugin) : ChunkGenerator() {
	override fun generateSurface(
		worldInfo: WorldInfo,
		random: Random,
		chunkX: Int,
		chunkZ: Int,
		chunkData: ChunkData
	) {
		if (chunkX == 0 && chunkZ == 0) {
			createStartIsland(chunkData)
		}
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
		return Location(world, 8.0, 64.0, 8.0)
	}

	private fun createStartIsland(chunkData: ChunkData) {
		// 7x7 island of dirt
		for (x in 4..10) {
			for (z in 4..10) {
				chunkData.setBlock(x, 64, z, Material.DIRT)
			}
		}
	}
}
