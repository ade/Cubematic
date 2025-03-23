package se.ade.mc.skyblock.generator.nether

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.AdeSkyblockPlugin
import java.util.*

class NetherPopulator(private val plugin: AdeSkyblockPlugin) : BlockPopulator() {
	override fun populate(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, region: LimitedRegion) {
		if (chunkX == 0 && chunkZ == 0) {
			// Add crimson fungus
			region.setType(10, 65, 10, Material.CRIMSON_FUNGUS)

			// Place chest with nether starter items
			val chestX = 8
			val chestZ = 8
			val chestY = 65

			region.setType(chestX, chestY, chestZ, Material.CHEST)
			val blockData = Bukkit.createBlockData(Material.CHEST) as Directional
			blockData.facing = BlockFace.WEST
			region.setBlockData(chestX, chestY, chestZ, blockData)

			// Schedule task to fill the chest
			Bukkit.getScheduler().runTaskLater(plugin, Runnable {
				val world = plugin.server.getWorld(worldInfo.name)!!
				val chestBlock = world.getBlockAt(chestX, chestY, chestZ)

				if (chestBlock.type == Material.CHEST) {
					val chestState = chestBlock.state as Chest
					val inventory = chestState.inventory
					inventory.clear()
					inventory.addItem(ItemStack(Material.FLINT_AND_STEEL, 1))
					inventory.addItem(ItemStack(Material.GOLD_INGOT, 3))
					inventory.addItem(ItemStack(Material.OBSIDIAN, 2))
				}
			}, 1)
		}
	}
}