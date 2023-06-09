package se.ade.mc.cubematic.portals

import se.ade.mc.cubematic.CubematicPlugin
import se.ade.mc.cubematic.facingBlock
import se.ade.mc.cubematic.toDispenser
import org.bukkit.Axis
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Container
import org.bukkit.block.EndGateway
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Orientable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.entity.EntityPortalEnterEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerPortalEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta
import se.ade.mc.cubematic.ForkCompat
import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.cubematic.extensions.getCenter
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

private val flatNeighbors = arrayOf(
    BlockFace.NORTH,
    BlockFace.EAST,
    BlockFace.SOUTH,
    BlockFace.WEST,
    BlockFace.NORTH_EAST,
    BlockFace.NORTH_WEST,
    BlockFace.SOUTH_EAST,
    BlockFace.SOUTH_WEST,
)

private const val ALLOW_NON_PLAYER_ENTITIES = false
private const val IMMEDIATE_MODE = true
private const val MAX_PORTAL_SIZE = 21

private val portalMaterial = Material.END_GATEWAY
private val frameMaterial = Material.CRYING_OBSIDIAN

class PortalTestAspect(private val cubematic: CubematicPlugin): Listener, Aspect(cubematic) {
    private val debug = true
    val logger: Logger? = if(debug) cubematic.logger else null

    init {
        ForkCompat.handleEntityEndGatewayTeleport(this) { entityTeleportEvent, endGateway ->
            entityTeleportEvent.isCancelled = true
            cubematic.server.broadcastMessage("${entityTeleportEvent.entity.type}, ${endGateway.location}")
        }
    }

    @EventHandler
    fun onEvent(e: EntityPortalEnterEvent) {
        if(IMMEDIATE_MODE && e.entity is Player && e.location.block.type == portalMaterial) {
            onPortalActivated(e.location, e.entity as Player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onEvent(e: PlayerPortalEvent) {
        if(!IMMEDIATE_MODE && e.cause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            e.isCancelled = onPortalActivated(e.player.location, e.player)
        }
    }

    @EventHandler
    fun onEvent(e: PlayerTeleportEvent) {
        cubematic.server.broadcastMessage("PlayerTeleportEvent: $e")
    }

    /*
    @EventHandler
    fun onEvent(e: EntityTeleportEvent) {
        plugin.server.broadcastMessage("EntityTeleportEvent: $e")
    }

     */

    private fun onPortalActivated(location: Location, player: Player): Boolean {
        val portalStart = if(location.block.type == portalMaterial) {
            location.block
        } else {
            location.block.getNeighborsCubic(1)
                .filter { it.type == portalMaterial }
                .map { it.location.getCenter() }
                .nearestOrNull(location)
                ?.block
        }

        if(portalStart == null)
            return false

        val portalBlocks = portalStart.findAllChaining(portalMaterial)

        val compass = findPortalCompass(portalBlocks)
            ?: return false

        val dest = compass.lodestone?.clone()?.add(0.5, 1.0, 0.5)
            ?: return false

        if(player.foodLevel >= FOOD_BURN + 1) {
            player.foodLevel -= FOOD_BURN

            cubematic.scheduleRun {
                player.teleport(dest, PlayerTeleportEvent.TeleportCause.PLUGIN)
            }
        } else {
            player.playSound(player, Sound.ENTITY_PLAYER_BURP, 1f, 1f)
        }

        portalBlocks.forEach { it.type = Material.AIR }
        return true
    }

    private fun findPortalCompass(portalBlocks: Set<Block>): CompassMeta? {
        val portalCreatorCandidates = portalBlocks.flatMap { block ->
            adjacentFaces.map { face ->
                val rel = block.getRelative(face)
                if(rel.type == Material.DISPENSER || rel.type == Material.DROPPER) {
                    rel
                } else {
                    null
                }
            }
        }.filterNotNull()

        val connectedDispensers = portalCreatorCandidates.flatMap { block ->
            adjacentFaces.map { face ->
                val rel = block.getRelative(face)
                val facing = rel.facingBlock()

                if(rel.type == Material.DISPENSER
                    && rel in portalCreatorCandidates
                    && (facing?.type == Material.DROPPER || facing?.type == Material.DISPENSER)) {
                        rel
                } else {
                    null
                }
            }
        }.filterNotNull()

        val allToSearch = (portalCreatorCandidates + connectedDispensers).toSet()

        allToSearch.forEach { block ->
            adjacentFaces.forEach { face ->
                val rel = block.getRelative(face)
                if(rel.type == Material.DISPENSER) {
                    return rel.toDispenser().inventory.filterNotNull().filter {
                        it.type == Material.COMPASS && (it.itemMeta as CompassMeta).isLodestoneTracked
                    }.randomOrNull()?.itemMeta as? CompassMeta
                }
            }
        }

        return null
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
        val target = (gwStartBlock.state as EndGateway).exitLocation?.clone()?.add(0.0, -1.0, 0.0)

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
        val logger: Logger? = if(debug) cubematic.logger else null

        val portalBlocks = findValidPortalBlocks(block)
            ?: return false

        if(portalBlocks.blocks.isEmpty())
            return false

        portalBlocks.blocks.forEach {
            it.type = portalMaterial

            if(portalMaterial == Material.END_GATEWAY) {
                (it.state as EndGateway).also { gw ->
                    gw.isExactTeleport = true
                    gw.exitLocation = compass.lodestone?.clone()?.add(0.0, 1.0, 0.0)
                    gw.age = Long.MIN_VALUE
                    gw.update()
                }
            }
        }

        return true
    }

    private fun findValidPortalBlocks(start: Block): PortalBlocks? {
        val logger: Logger? = if(debug) cubematic.logger else null

        val frameType = frameMaterial

        val frameTypes = listOf(frameType)
        val up = start.findFirst(frameTypes, BlockFace.UP, MAX_PORTAL_SIZE)
            ?: return null

        val down = start.findFirst(frameTypes, BlockFace.DOWN, MAX_PORTAL_SIZE)
            ?: return null

        val startColumn = down.column(up)
            ?: return null

        val startColumnContent = startColumn.drop(1).dropLast(1)
        if(startColumnContent.isEmpty() || startColumnContent.any { it.type != Material.AIR }) {
            logger?.info("Column is empty or invalid")
            return null
        }

        val north = expandPortal(startColumn, BlockFace.NORTH, frameTypes)
        val south = expandPortal(startColumn, BlockFace.SOUTH, frameTypes)

        if(north != null && south != null) {
            return PortalBlocks(axis = Axis.Z, blocks = north + startColumnContent + south)
        }

        val west = expandPortal(startColumn, BlockFace.WEST, frameTypes)
        val east = expandPortal(startColumn, BlockFace.EAST, frameTypes)

        if(west != null && east != null) {
            return PortalBlocks(axis = Axis.X, blocks = west + startColumnContent + east)
        }

        return null
    }

    private fun expandPortal(column: List<Block>, direction: BlockFace, frameTypes: List<Material>): List<Block>? {
        val allBlocks = mutableListOf<Block>()

        var start = column.first()
        var end = column.last()
        (1..20).forEach {
            start = start.getRelative(direction)
            end = end.getRelative(direction)

            val col = start.column(end)
                ?: return null

            val contents = col.drop(1).dropLast(1)
            val isAirInside = contents.all { it.type == Material.AIR }
            val isFrameInside = contents.all { it.type in frameTypes }

            when {
                start.type in frameTypes && end.type in frameTypes && isAirInside -> {
                    allBlocks.addAll(contents)
                }
                isFrameInside -> {
                    return allBlocks
                }
                else -> return null
            }
        }

        return null
    }
}

private fun List<Location>.nearestOrNull(location: Location): Location? {
    return if(this.isEmpty())
        null
    else
        this.minBy { location.distance(it) }
}

fun Block.getNeighborsCubic(distance: Int): List<Block> {
    val blocks = mutableListOf<Block>()
    (-distance..distance).forEach { x ->
        (-distance..distance).forEach { y ->
            (-distance..distance).forEach { z ->
                if(x != 0 || y != 0 || z != 0)
                    blocks.add(this.getRelative(x,y,z))
            }
        }
    }

    return blocks
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

fun Block.findFirst(type: List<Material>, direction: BlockFace, limit: Int): Block? {
    var block = this
    (1..limit).forEach { _ ->
        block = block.getRelative(direction)

        if (block.type in type)
            return block
    }
    return null
}

fun Block.column(that: Block): List<Block>? {
    if(that.x != this.x || that.z != this.z)
        return null

    return (this.y..that.y).map {
        this.world.getBlockAt(this.x, it, this.z)
    }
}

data class PortalBlocks(val axis: Axis, val blocks: List<Block>)