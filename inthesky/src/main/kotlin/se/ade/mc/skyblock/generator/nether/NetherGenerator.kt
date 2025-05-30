package se.ade.mc.skyblock.generator.nether

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.util.*

class NetherGenerator(private val plugin: CubematicSkyPlugin) : ChunkGenerator() {
	override fun generateSurface(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, chunkData: ChunkData) {
		if (chunkX == 0 && chunkZ == 0) {
			//createNetherIsland(chunkData, random)
		}
	}

	override fun getDefaultPopulators(world: World): List<BlockPopulator?> {
		return listOf()
		//return listOf(NetherPopulator(plugin))
	}

	override fun getFixedSpawnLocation(world: World, random: Random): Location? {
		// You can't respawn in the nether, so we return null
		return null
	}

	override fun getDefaultBiomeProvider(worldInfo: WorldInfo): BiomeProvider? {
		// TODO Configuration opportunity (use custom or not)
		// return NetherBiomeProvider(0, 0, supportedNetherBiomes)
		return null
	}

	override fun shouldGenerateStructures(): Boolean {
		// This will generate the metadata fortress "structure" without the blocks
		return true
	}

	override fun shouldGenerateDecorations(): Boolean {
		// This will generate nether fortresses and more
		return false
	}
}