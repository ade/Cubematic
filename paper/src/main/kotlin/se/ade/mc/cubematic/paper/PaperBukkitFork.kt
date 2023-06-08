package se.ade.mc.cubematic.paper

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent
import org.bukkit.block.Block
import org.bukkit.block.EndGateway
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import se.ade.mc.cubematic.BukkitFork
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.extensions.listenTo


class PaperBukkitFork: BukkitFork {
    override fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float {
        return PaperApis.getBlockDestroySpeed(block, tool)
    }

    override fun isBlockReplaceable(block: Block): Boolean {
        return PaperApis.isBlockReplaceable(block)
    }

    override fun handleEntityEndGatewayTeleport(aspect: Aspect, handler: (EntityTeleportEvent, EndGateway) -> Unit) {
        /*
        plugin.server.pluginManager.registerEvents(object: Listener {
            @EventHandler
            fun onEvent(e: EntityTeleportEndGatewayEvent) {
                handler(e, e.gateway)
            }
        }, plugin)

         */

        aspect.listenTo<EntityTeleportEndGatewayEvent> { e ->
            handler(e, e.gateway)
        }
    }
}