package se.ade.mc.cubematic

import se.ade.mc.cubematic.breaking.BreakerAspect
import se.ade.mc.cubematic.placing.PlacingAspect
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.config.CubeConfig
import se.ade.mc.cubematic.config.configProvider

class CubematicAutomationPlugin: JavaPlugin() {
    val config: CubeConfig by configProvider { CubeConfig() }

    val namespaceKeys = Namespaces(
        craftingDropper = createNamespacedKey("crafting_dropper"),
        dropSlot = createNamespacedKey("drop_slot"),
        placerBlockTag = createNamespacedKey("placer")
    )
    override fun onEnable() {
        super.onEnable()
        //server.pluginManager.registerEvents(DebugAspect(this), this)

        server.pluginManager.registerEvents(BreakerAspect(this), this)
        server.pluginManager.registerEvents(PlacingAspect(this), this)

        if(config.debug) {
            logger.info("Initialized with debug mode enabled")
        }
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