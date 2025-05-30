package se.ade.mc.skyblock.generator.nether

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.block.data.Directional
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.inventory.ItemStack
import se.ade.mc.skyblock.CubematicSkyPlugin
import java.util.Random

// TODO Configuration opportunity
private val islandY = 64

/**
 * Creates a 7x7 netherrack island with a 5-block deep platform.
 */
fun createNetherIsland(chunkData: ChunkGenerator.ChunkData, random: Random) {
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

fun populateNetherIsland(plugin: CubematicSkyPlugin, worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int, region: LimitedRegion) {
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