package se.ade.mc.cubematic

import org.bukkit.block.Block
import org.bukkit.block.EndGateway
import org.bukkit.inventory.ItemStack
import org.bukkit.event.entity.EntityTeleportEvent
import se.ade.mc.cubematic.extensions.Aspect

interface BukkitFork {
    fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float
    fun isBlockReplaceable(block: Block): Boolean

    fun handleEntityEndGatewayTeleport(aspect: Aspect, handler: (EntityTeleportEvent, EndGateway) -> Unit)
}