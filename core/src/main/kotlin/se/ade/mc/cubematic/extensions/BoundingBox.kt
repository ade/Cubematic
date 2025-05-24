package se.ade.mc.cubematic.extensions

import org.bukkit.Location
import org.bukkit.util.BoundingBox

fun BoundingBox.containsInclusive(x: Int, y: Int, z: Int): Boolean {
	return x >= minX.toInt() && x <= maxX.toInt() &&
			y >= minY.toInt() && y <= maxY.toInt() &&
			z >= minZ.toInt() && z <= maxZ.toInt()
}

fun BoundingBox.containsInclusive(location: Location): Boolean {
	return containsInclusive(location.blockX, location.blockY, location.blockZ)
}