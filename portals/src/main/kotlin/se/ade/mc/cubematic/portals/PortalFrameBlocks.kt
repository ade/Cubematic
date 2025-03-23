package se.ade.mc.cubematic.portals

import org.bukkit.Material
import org.bukkit.block.Block

data class PortalFrameBlocks(
    val model: PortalFrame,
    val frame: List<Block>,
    val inside: List<Block>
) {
    fun consistsOf(frameMaterial: Material, insideMaterial: Material): Boolean {
        return frame.all { it.type == frameMaterial } && inside.all { it.type == insideMaterial }
    }
}