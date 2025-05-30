package se.ade.mc.skyblock.interaction

import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPistonExtendEvent

/**
 * When cobblestone is pushed into lava (source or flowing) with a piston,
 * it should be converted to netherrack in the Nether or deepslate in the Overworld.
 */
object PushBlockInLavaRule: Listener {
    @EventHandler
    fun onPistonExtend(event: BlockPistonExtendEvent) {
        // For each block being pushed
        for ((i, block) in event.blocks.withIndex()) {
            // Only care about cobblestone
            if (block.type == Material.COBBLESTONE) {
                // The block in front of the cobblestone after the push
                val direction = event.direction
                val targetBlock = block.getRelative(direction)

	            // Check if the target block is lava (source or flowing)
                if (targetBlock.type == Material.LAVA) {
                    when {
                        event.block.world.environment == World.Environment.NETHER -> {
                            block.type = Material.NETHERRACK
                        }
                        event.block.world.environment == World.Environment.NORMAL -> {
                            block.type = Material.DEEPSLATE
                        }
                    }
                }
            }
        }
    }
}