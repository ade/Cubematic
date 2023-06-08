package se.ade.mc.cubematic.extensions

import org.bukkit.Location

fun Location.getCenter(): Location {
    return clone().also {
        it.x = blockX + 0.5
        it.y = blockY + 0.5
        it.z = blockZ + 0.5
    }
}

fun Location.setCompat(x: Double, y: Double, z: Double) = this.apply {
    this.x = x
    this.y = y
    this.z = z
}