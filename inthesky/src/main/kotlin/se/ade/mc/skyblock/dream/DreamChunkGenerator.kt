package se.ade.mc.skyblock.dream

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random

internal class DreamChunkGenerator : ChunkGenerator() {
    override fun getDefaultPopulators(world: World): List<BlockPopulator> {
        return listOf()
    }

    override fun generateNoise(
	    worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int,
	    chunkData: ChunkData
    ) {
        // No need to generate noise, we want an empty world
    }

    override fun generateSurface(
	    worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int,
	    chunkData: ChunkData
    ) {
		if(chunkX == 0 || chunkZ == 0) {
			chunkData.setRegion(0, 63, 0, 16, 64, 16, Material.BEDROCK)
		}
    }

    override fun generateBedrock(
	    worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int,
	    chunkData: ChunkData
    ) {

    }

    override fun generateCaves(
	    worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int,
	    chunkData: ChunkData
    ) {
        // No need to generate caves, we want an empty world
    }

    override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
        return EndBiomeProvider()
    }

    override fun canSpawn(world: World, x: Int, z: Int): Boolean {
        return true
    }

    override fun getFixedSpawnLocation(world: World, random: Random): Location {
        return Location(world, 8.0, 65.0, 8.0)
    }
}