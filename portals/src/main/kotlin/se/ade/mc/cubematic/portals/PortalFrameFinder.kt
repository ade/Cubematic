package se.ade.mc.cubematic.portals

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

sealed class PortalPlane(val directions: List<BlockFace>) {
    object XYPlane : PortalPlane(listOf(BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN))
    object ZYPlane : PortalPlane(listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN))
    object XZPlane : PortalPlane(listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST))
}

/**
 * Finds a frame-structure (nether portal style)
 * @param frameMaterial The material of the outside frame
 * @param origin the first block to start searching from. Must be a block inside the frame (typically an air block)
 * @param limit the maximum inside width/height of the portal.
 * @param allowActive if true, the frame may contain active portal blocks
 */
class PortalFrameFinder(
    private val frameMaterial: Material,
    private val origin: Block,
    private val limit: Int,
    private val allowActive: Boolean = false) {

    /**
     * Searches outwards for a portal frame in all three planes and returns it if found.
     */
    fun find(): PortalFrameBlocks? {
        return searchPlane(PortalPlane.XYPlane)
            ?: searchPlane(PortalPlane.ZYPlane)
            ?: searchPlane(PortalPlane.XZPlane)
    }

    private fun validateOrNull(frame: PortalFrame): PortalFrameBlocks? {
        if(frame.maxSize() > limit)
            return null

        val frameBlocks = frame.blocks()

        val isActivePortal = frameBlocks.consistsOf(frameMaterial, Material.END_GATEWAY)

        if(!frameBlocks.consistsOf(frameMaterial, Material.AIR) && !(allowActive && isActivePortal))
            return null

        return frameBlocks
    }

    private fun searchPlane(portalPlane: PortalPlane): PortalFrameBlocks? {
        val edges = portalPlane.directions.mapNotNull {
            findFrameInsideEdge(it)
        }

        if(edges.size != 4)
            return null

        return validateOrNull(PortalFrame(
            minX = edges.minOf { it.x },
            maxX = edges.maxOf { it.x },
            minY = edges.minOf { it.y },
            maxY = edges.maxOf { it.y },
            minZ = edges.minOf { it.z },
            maxZ = edges.maxOf { it.z },
            portalPlane = portalPlane,
            world = origin.world
        ))
    }

    /** Returns the first block of frame material in the search direction */
    private fun findFrameInsideEdge(direction: BlockFace): Block? {
        var currentBlock = origin.getRelative(direction)

        for (i in 1 .. limit) {
            if (currentBlock.type == frameMaterial) {
                return currentBlock
            }
            currentBlock = currentBlock.getRelative(direction)
        }

        return null
    }

}