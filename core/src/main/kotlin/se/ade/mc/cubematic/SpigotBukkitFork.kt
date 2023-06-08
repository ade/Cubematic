package se.ade.mc.cubematic

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.EndGateway
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.nms.destroyspeed.DestroySpeedShimFactory

class SpigotBukkitFork: BukkitFork {
    private val destroySpeedShim by lazy {
        DestroySpeedShimFactory.create()
    }

    private val replaceableBlocks = mapOf(
        Material.AIR to true,
        Material.WATER to true,
        Material.LAVA to true,
        Material.GRASS to true,
        //TODO -> Flowers? more stuff.
    )

    override fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float {
        return try {
            destroySpeedShim.getDestroySpeed(block, tool)
        } catch (t: Throwable) {
            1.0f
        }
    }

    override fun isBlockReplaceable(block: Block): Boolean {
        return replaceableBlocks.getOrDefault(block.type, false)
    }

    override fun handleEntityEndGatewayTeleport(aspect: Aspect, handler: (EntityTeleportEvent, EndGateway) -> Unit) {
        //Not possible, will never call handler
    }
}