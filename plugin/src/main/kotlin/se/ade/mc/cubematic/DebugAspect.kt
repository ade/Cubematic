package se.ade.mc.cubematic

import se.ade.mc.cubematic.paper.PaperApis
import se.ade.mc.cubematic.rules.BlockBreaking
import se.ade.mc.cubematic.rules.Rules
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import se.ade.mc.cubematic.nms.destroyspeed.DestroySpeedShimFactory

class DebugAspect(private val plugin: CubematicPlugin): Listener {
    private val destroySpeedShim by lazy {
        DestroySpeedShimFactory.create()
    }
    @EventHandler
    fun onEvent(event: PlayerInteractEvent) {
        if(event.clickedBlock != null && event.item?.type == Material.STICK) {
            val type = event.clickedBlock?.type
            val loot = event.clickedBlock?.getDrops(ItemStack(Material.WOODEN_PICKAXE))
            val time = event.clickedBlock?.let {
                PaperApis.getBlockDestroySpeed(it, ItemStack(Material.WOODEN_PICKAXE))
            }

            plugin.server.broadcastMessage("Hardness ${type?.hardness}, requireSpecial: ${event.clickedBlock?.blockData?.requiresCorrectToolForDrops()}, loot: $loot, time: $time")
        }

        if(event.clickedBlock != null && event.item?.type in Rules.ALL_TOOLS) {
            val block = event.clickedBlock!!
            val tool = event.item!!

            val breakTime = BlockBreaking.getBreakTimeTicks(event.item!!, event.clickedBlock!!) / 20 //to seconds
            val requireSpecial = event.clickedBlock?.blockData?.requiresCorrectToolForDrops()
            val isPreferred = event.clickedBlock!!.isPreferredTool(event.item!!)

            val nativeDestroySpeed = try {
                PaperApis.getBlockDestroySpeed(block, tool).toString()
            } catch (e: Throwable) {
                //Doesn't work in non-paper (spigot)
                "<error>"
            }
            val shimDestroySpeed = destroySpeedShim.getDestroySpeed(block, tool)
            val canHarvest = !block.blockData.requiresCorrectToolForDrops() || block.blockData.isPreferredTool(tool)
            val efficiencyLevel = tool.itemMeta?.enchants?.firstNotNullOfOrNull {
                if(it.key == Enchantment.DIG_SPEED)
                    it.value
                else null
            } ?: 0
            val blockHardness = block.type.hardness

            plugin.server.broadcastMessage("Breaktime: $breakTime. requireSpecial: $requireSpecial, isPreferred: $isPreferred, canHarvest: $canHarvest, hardness: $blockHardness, nativeDestroySpeed: $nativeDestroySpeed, shimDestroySpeed: $shimDestroySpeed, efficiency: $efficiencyLevel")
        }
    }
}