package se.ade.mc.cubematic.extensions

import org.bukkit.block.Block
import org.bukkit.block.BlockFace

fun Block.adjacentHorizontal(): List<Block> {
	return listOf(
		getRelative(BlockFace.NORTH),
		getRelative(BlockFace.SOUTH),
		getRelative(BlockFace.EAST),
		getRelative(BlockFace.WEST)
	)
}

fun Block.isRightTemperatureForRain() =
	temperature in 0.15..0.95