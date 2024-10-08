package se.ade.mc.cubematic.portals

import com.destroystokyo.paper.event.entity.EntityTeleportEndGatewayEvent
import se.ade.mc.cubematic.CubematicPlugin
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.EndGateway
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.util.Vector
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.extensions.listenTo
import java.util.logging.Logger

private const val FOOD_BURN = 4

private val adjacentFaces = arrayOf(
    BlockFace.DOWN,
    BlockFace.UP,
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST
)

private const val ALLOW_NON_PLAYER_ENTITIES = false
private const val MAX_PORTAL_SIZE = 21

private val portalMaterial = Material.END_GATEWAY
private val frameMaterial = Material.CRYING_OBSIDIAN
private val targetOffset = Vector(0.0, 1.0, 0.0)

class PortalAspect(private val cubematic: CubematicPlugin): Listener, Aspect(cubematic) {
    private val debug = cubematic.config.debug
    private val logger: Logger? = if(debug) cubematic.logger else null

    init {
        listenTo<EntityTeleportEndGatewayEvent> {
            //Disable entities teleporting through the gateway, if configured so
            if (ALLOW_NON_PLAYER_ENTITIES) return@listenTo

            it.isCancelled = true
            logger?.info("Cancel entity teleport: ${it.entity.type}, ${it.gateway.location}")
        }
    }

    @EventHandler
    fun onEvent(e: PlayerTeleportEvent) {
        logger?.info("PlayerTeleportEvent: ${e.player}, ${e.cause}, from ${e.from}, to ${e.to}")
        if(FOOD_BURN > 0) {
            if(e.player.foodLevel >= FOOD_BURN + 1) {
                e.player.foodLevel -= FOOD_BURN
            } else {
                e.player.playSound(e.player, Sound.ENTITY_PLAYER_BURP, 1f, 1f)
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEvent(event: BlockBreakEvent) {
        if(event.block.type != Material.CRYING_OBSIDIAN)
            return

        val gateways = adjacentFaces.mapNotNull {
            val block = event.block.getRelative(it)
            if(block.type == Material.END_GATEWAY)
                block
            else
                null
        }

        if(gateways.isEmpty())
            return

        //Todo: Find disjoint groups of gateway blocks
        val gwStartBlock = gateways.first()
        val target = (gwStartBlock.state as EndGateway).exitLocation?.clone()?.subtract(targetOffset)

        logger?.info("Target: $target")

        val allBlocks = gwStartBlock.findAllChaining(Material.END_GATEWAY)
        allBlocks.forEach {
            it.type = Material.AIR
        }

        if(target != null) {
            val compass = ItemStack(Material.COMPASS)

            compass.itemMeta = (compass.itemMeta as CompassMeta).also {
                it.lodestone = target.clone()
                it.isLodestoneTracked = true
                it.addEnchant(Enchantment.VANISHING_CURSE, 1, true)
            }

            logger?.info(compass.itemMeta.toString())
            gwStartBlock.world.dropItem(gwStartBlock.location, compass)
        }
    }

    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val item = event.player.inventory.itemInMainHand ?: return
        val block = event.clickedBlock ?: return

        if(item.type == Material.COMPASS && block.type == Material.LODESTONE) {
            cubematic.scheduleRun {
                cubematic.server.broadcastMessage(item.itemMeta.toString())
            }
        }

        if(item.type == Material.COMPASS && block.type == frameMaterial) {
            val compass = item.itemMeta as CompassMeta
            if(!compass.isLodestoneTracked)
                return

            if(!item.enchantments.containsKey(Enchantment.VANISHING_CURSE))
                return

            val activationBlock = block.getRelative(event.blockFace)
            event.isCancelled = true
            if(activateFrame(activationBlock, compass)) {
                event.player.inventory.setItemInMainHand(null)
            }
        }
    }

    private fun activateFrame(block: Block, compass: CompassMeta): Boolean {
        val portalFrame = PortalFrameFinder(frameMaterial, block, MAX_PORTAL_SIZE).find()

        if(portalFrame == null) {
            logger?.info("portalBlocks null")
            return false
        }

        logger?.info("Frame: ${portalFrame.model}")

        portalFrame.inside.forEach {
            it.type = portalMaterial

            if(portalMaterial == Material.END_GATEWAY) {
                (it.state as EndGateway).also { gw ->
                    gw.isExactTeleport = true
                    gw.exitLocation = compass.lodestone?.clone()?.add(targetOffset)
                    gw.age = Long.MIN_VALUE
                    gw.update()
                }
            }
        }

        return true
    }
}

fun Block.findAllChaining(type: Material): Set<Block> {
    val results = mutableSetOf<Block>()
    val checked = mutableSetOf<Block>()
    val todo = mutableSetOf<Block>()
    todo.add(this)
    results.add(this)
    do {
        val newBlocks = todo.flatMap { current ->
            if (checked.contains(current)) return@flatMap listOf<Block>()
            checked.add(current)

            adjacentFaces.mapNotNull { face ->
                val candidate = current.getRelative(face)

                when (candidate.type) {
                    type -> candidate
                    else -> null
                }
            }
        }

        todo.clear()
        results.addAll(newBlocks)
        todo.addAll(newBlocks)

    } while (todo.isNotEmpty())
    return results
}