package se.ade.mc.cubematic.paper

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import se.ade.mc.cubematic.BukkitFork

class PaperBukkitFork: BukkitFork {
    override fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float {
        return PaperApis.getBlockDestroySpeed(block, tool)
    }

    override fun isBlockReplaceable(block: Block): Boolean {
        return PaperApis.isBlockReplaceable(block)
    }
}