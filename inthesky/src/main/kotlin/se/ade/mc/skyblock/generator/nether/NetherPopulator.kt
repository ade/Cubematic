package se.ade.mc.skyblock.generator.nether

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.util.*

class NetherPopulator(private val plugin: CubematicSkyPlugin) : BlockPopulator() {
	override fun populate(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, region: LimitedRegion) {
		if (chunkX == 0 && chunkZ == 0) {
			populateNetherIsland(plugin, worldInfo, random, chunkX, chunkZ, region)
		}
	}
}