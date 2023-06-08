package se.ade.mc.cubematic

import org.bukkit.block.Block
import org.bukkit.block.EndGateway
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.paper.PaperBukkitFork

object ForkCompat: BukkitFork {
    private val isPaper: Boolean by lazy {
        ClassLoader.getSystemClassLoader().let {
            it.getResource("io/papermc/paper") != null
                    || it.getResource("io/papermc/paperclip") != null
                    || it.getResource("com/destroystokyo/paper") != null
                    || it.getResource("com/destroystokyo/paperclip") != null
        }
    }

    private val fork: BukkitFork by lazy {
        when {
            isPaper -> PaperBukkitFork()
            else -> SpigotBukkitFork()
        }
    }

    override fun getBlockDestroySpeed(block: Block, tool: ItemStack): Float
        = fork.getBlockDestroySpeed(block, tool)
    override fun isBlockReplaceable(block: Block): Boolean
        = fork.isBlockReplaceable(block)

    override fun handleEntityEndGatewayTeleport(aspect: Aspect, handler: (EntityTeleportEvent, EndGateway) -> Unit)
        = fork.handleEntityEndGatewayTeleport(aspect, handler)
}