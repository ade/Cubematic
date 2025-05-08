package se.ade.mc.skyblock

import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.config.configProvider
import se.ade.mc.skyblock.interaction.InteractionFacet
import se.ade.mc.skyblock.generator.GeneratorSelector
import se.ade.mc.skyblock.nether.NetherFacet
import se.ade.mc.skyblock.trader.TraderFacet

class CubematicSkyPlugin: JavaPlugin(), Listener {
    var config by configProvider { SkyConfig() }
    val netherFacet = NetherFacet(this)
    val traderFacet = TraderFacet(this)
    val interactionFacet = InteractionFacet(this)

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        netherFacet.onEnable()
        traderFacet.enable()
        interactionFacet.enable()
        //testGraphWithPlugin(this)
    }

    override fun onDisable() {
        super.onDisable()

        netherFacet.onDisable()
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return GeneratorSelector.selectGenerator(this, worldName, id)
    }
}

