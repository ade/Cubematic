package se.ade.mc.skyblock

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.skyblock.dream.DreamFacet
import se.ade.mc.skyblock.generator.GeneratorSelector
import se.ade.mc.skyblock.nether.NetherFacet

class SkyblockListener(private val plugin: AdeSkyblockPlugin): Listener {
    @EventHandler
    fun onEvent(event: WorldLoadEvent) {
        if(event.world.name == "world") {
            plugin.dreamFacet = DreamFacet(plugin, event.world).also { it.onEnable() }
        }
    }
}

class AdeSkyblockPlugin: JavaPlugin(), CommandHandler {
    var dreamFacet: DreamFacet? = null
    val commandRegistrar = CommandRegistrar(this)
    val netherFacet = NetherFacet(this)

    override fun onEnable() {
        server.pluginManager.registerEvents(SkyblockListener(this), this)
        commandRegistrar.register(this)
        dreamFacet?.onEnable()
        netherFacet.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
        dreamFacet?.onDisable()
        netherFacet.onDisable()
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return GeneratorSelector.selectGenerator(this, worldName, id)
    }

    override fun onCreateDreamWorldCommand() {
        dreamFacet?.onCreateDreamWorldCommand()
    }

    override fun onEnterDreamWorldCommand(player: Player) {
        dreamFacet?.onEnterDreamWorldCommand(player)
    }

    override fun onLeaveDreamWorldCommand(player: Player) {
        dreamFacet?.onLeaveDreamWorldCommand(player)
    }

    override fun onDestroyDreamWorldCommand() {
        dreamFacet?.onDestroyDreamWorldCommand()
    }

    override fun onStashInventoryCommand(player: Player) {
        dreamFacet?.onStashInventoryCommand(player)
    }

    override fun onPopInventoryCommand(player: Player) {
        dreamFacet?.onPopInventoryCommand(player)
    }
}

