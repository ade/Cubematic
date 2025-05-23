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

/**
 * Returns the internal skylight value of a block.
 * https://minecraft.wiki/w/Light#Internal_sky_light
 */
fun Block.getInternalSkylight(): Int {
	val isNight = world.time in 13000..23999

	val internalSkyLightAt15 = when {
		isNight -> 4
		world.isThundering -> 10
		world.hasStorm() -> 12
		else -> 15
	}

	return (internalSkyLightAt15 - (15 - lightFromSky)).coerceAtLeast(0)
}

/**
 * Returns true if the block is a valid spawn location for "regular" hostile mobs,
 * at this instant.
 * Does not take into account:
 *  - the required vertical space above the block
 *  - special rules for specific mobs
 *  - the biome
 *  - anything else I forgot
 */
fun Block.hostileMobSpawnable(): Boolean {
	if (type.isSolid) return false
	if (type.isOccluding) return false
	if (isLiquid) return false
	if (!getRelative(BlockFace.DOWN).isSolid) return false
	if (lightFromBlocks > 0) return false
	if (getInternalSkylight() > 7) return false

	return true
}