package se.ade.mc.cubematic

import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

interface BukkitFork {
    fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float
    fun isBlockReplaceable(block: Block): Boolean
}