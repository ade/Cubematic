package se.ade.mc.skyblock.generator.overworld

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.CubeInTheSkyPlugin
import java.util.Random

private const val islandY = 64

class TreePopulator(private val plugin: CubeInTheSkyPlugin) : BlockPopulator() {
	override fun populate(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, region: LimitedRegion) {
		// Only add the tree in the spawn chunk
		if (chunkX == 0 && chunkZ == 0) {
			// Generate larger tree
			val treeX = 8
			val treeZ = 11
			val treeBaseY = islandY + 1

			// Create tree trunk (taller)
			for (y in treeBaseY until treeBaseY + 5) {
				region.setType(treeX, y, treeZ, Material.OAK_LOG)
			}

			// Create larger leaf canopy
			// Main canopy layer
			for (y in treeBaseY + 3..treeBaseY + 5) {
				for (x in treeX - 2..treeX + 2) {
					for (z in treeZ - 2..treeZ + 2) {
						// Skip corners for a more natural round shape
						if ((x == treeX - 2 && z == treeZ - 2) || (x == treeX - 2 && z == treeZ + 2) ||
							(x == treeX + 2 && z == treeZ - 2) || (x == treeX + 2 && z == treeZ + 2)) {
							continue
						}

						// Don't overwrite the trunk
						if (x == treeX && z == treeZ && y < treeBaseY + 5) {
							continue
						}

						region.setType(x, y, z, Material.OAK_LEAVES)
					}
				}
			}

			// Top leaf layer
			for (x in treeX - 1..treeX + 1) {
				for (z in treeZ - 1..treeZ + 1) {
					region.setType(x, treeBaseY + 6, z, Material.OAK_LEAVES)
				}
			}
			region.setType(treeX, treeBaseY + 7, treeZ, Material.OAK_LEAVES)

			// Place chest on a valid part of the L-shape
			val chestX = 11
			val chestZ = 8
			val chestY = islandY + 1

			// Create chest and set its direction
			region.setType(chestX, chestY, chestZ, Material.CHEST)
			val blockData = Bukkit.createBlockData(Material.CHEST) as Directional
			blockData.facing = BlockFace.WEST
			region.setBlockData(chestX, chestY, chestZ, blockData)

			// Schedule a task to fill the chest after chunk generation
			Bukkit.getScheduler().runTaskLater(plugin, Runnable {
				val world = plugin.server.getWorld(worldInfo.name)!!
				val chestBlock = world.getBlockAt(chestX, chestY, chestZ)

				if (chestBlock.type == Material.CHEST) {
					val chestState = chestBlock.state as Chest
					val inventory = chestState.inventory
					inventory.clear() // Clear any potential existing items
					inventory.addItem(ItemStack(Material.LAVA_BUCKET, 1))
					inventory.addItem(ItemStack(Material.ICE, 1))
				}
			}, 1) // Run one tick later to ensure chunk is loaded properly
		}
	}
}