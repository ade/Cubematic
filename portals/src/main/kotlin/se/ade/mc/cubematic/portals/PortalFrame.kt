package se.ade.mc.cubematic.portals

import org.bukkit.World
import org.bukkit.block.Block

data class PortalFrame(
    val minX: Int = 0,
    val maxX: Int = 0,
    val minY: Int = 0,
    val maxY: Int = 0,
    val minZ: Int = 0,
    val maxZ: Int = 0,
    val portalPlane: PortalPlane,
    val world: World
) {
    /**
     * Collects the inside and outside blocks and returns them.
     */
    fun blocks(): PortalFrameBlocks {
        val frame = mutableListOf<Block>()
        val inside = mutableListOf<Block>()

        when(portalPlane) {
            PortalPlane.XYPlane -> {
                for (x in minX..maxX) {
                    for (y in minY..maxY) {
                        
                        //Corners
                        if((x == minX && y == minY)
                            || (x == minX && y == maxY)
                            || (x == maxX && y == minY)
                            || (x == maxX && y == maxY)) {
                            continue
                        }
                        
                        val b = world.getBlockAt(x, y, minZ)
                        if(x == minX || x == maxX || y == minY || y == maxY)
                            frame.add(b)
                        else
                            inside.add(b)
                    }
                }
            }
            PortalPlane.ZYPlane -> {
                for (z in minZ..maxZ) {
                    for (y in minY..maxY) {

                        //Corners
                        if((z == minZ && y == minY)
                            || (z == minZ && y == maxY)
                            || (z == maxZ && y == minY)
                            || (z == maxZ && y == maxY)) {
                            continue
                        }
                        
                        val b = world.getBlockAt(minX, y, z)
                        if(z == minZ || z == maxZ || y == minY || y == maxY)
                            frame.add(b)
                        else
                            inside.add(b)
                    }
                }
            }
            PortalPlane.XZPlane -> {
                for (z in minZ..maxZ) {
                    for (x in minX..maxX) {

                        //Corners
                        if((x == minX && z == minZ)
                            || (x == minX && z == maxZ)
                            || (x == maxX && z == minZ)
                            || (x == maxX && z == maxZ)) {
                            continue
                        }
                        
                        val b = world.getBlockAt(x, minY, z)
                        if(z == minZ || z == maxZ || x == minX || x == maxX)
                            frame.add(b)
                        else
                            inside.add(b)
                    }
                }
            }
        }
        return PortalFrameBlocks(frame = frame, inside = inside, model = this)
    }

    fun maxSize(): Int {
        return listOf(
            this.maxX - this.minX - 2,
            this.maxY - this.minY - 2,
            this.maxZ - this.minZ - 2
        ).max()
    }
}
