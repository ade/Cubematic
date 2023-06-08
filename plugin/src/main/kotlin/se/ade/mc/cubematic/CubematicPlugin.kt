package se.ade.mc.cubematic

import se.ade.mc.cubematic.breaking.BreakerAspect
import se.ade.mc.cubematic.crafting.DispenseCraftingTableAspect
import se.ade.mc.cubematic.crafting.SequenceInputDropperAspect
import se.ade.mc.cubematic.placing.PlacingAspect
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.portals.PortalTestAspect

class CubematicPlugin: JavaPlugin() {
    val namespaceKeys = Namespaces(
        craftingDropper = createNamespacedKey("crafting_dropper"),
        dropSlot = createNamespacedKey("drop_slot"),
        placerBlockTag = createNamespacedKey("placer")
    )
    override fun onEnable() {
        super.onEnable()
        //server.pluginManager.registerEvents(DebugAspect(this), this)

        server.pluginManager.registerEvents(DispenseCraftingTableAspect(this), this)
        server.pluginManager.registerEvents(SequenceInputDropperAspect(this), this)

        server.pluginManager.registerEvents(BreakerAspect(this), this)
        server.pluginManager.registerEvents(PlacingAspect(this), this)

        server.pluginManager.registerEvents(PortalTestAspect(this), this)
        //server.pluginManager.registerEvents(ShriekerTest(this), this)
    }

    fun scheduleRun(delayTicks: Long = 0L, block: () -> Unit) {
        server.scheduler.runTaskLater(this, Runnable { block() }, delayTicks)
    }

    private fun createNamespacedKey(entry: String)
            = NamespacedKey.fromString("$namespace:$entry".lowercase(), this)!!
    companion object {
        const val namespace = "cubematic"
    }
}

data class Namespaces(
    val craftingDropper: NamespacedKey,
    val dropSlot: NamespacedKey,
    val placerBlockTag: NamespacedKey,
)