package se.ade.mc.skyblock

import org.bukkit.event.Listener
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.structure.Structure
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.config.configProvider
import se.ade.mc.cubematic.extensions.commands
import se.ade.mc.skyblock.structuremaps.createStructureMap
import se.ade.mc.skyblock.interaction.InteractionFacet
import se.ade.mc.skyblock.generator.GeneratorSelector
import se.ade.mc.skyblock.mobs.MobsFacet
import se.ade.mc.skyblock.nether.NetherFacet
import se.ade.mc.skyblock.nether.createNetherFortressMap
import se.ade.mc.skyblock.structuremaps.StructureMapsFacet
import se.ade.mc.skyblock.trader.TraderFacet

class CubematicSkyPlugin: JavaPlugin(), Listener {
    var config by configProvider { SkyConfig() }
    val netherFacet = NetherFacet(this)
    val traderFacet = TraderFacet(this)
    val mobsFacet = MobsFacet(this)
    val interactionFacet = InteractionFacet(this)
    val structureMapsFacet = StructureMapsFacet(this)

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)
        netherFacet.onEnable()
        traderFacet.enable()
        mobsFacet.enable()
        interactionFacet.enable()
        structureMapsFacet.enable()

        //testGraphWithPlugin(this)

        addCommands(this)
    }

    override fun onDisable() {
        super.onDisable()

        netherFacet.onDisable()
    }

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        return GeneratorSelector.selectGenerator(this, worldName, id)
    }

    private fun addCommands(plugin: CubematicSkyPlugin) {
        commands {
            command("cubematic") {
                subcommand("sky") {
                    subcommand("debug") {
                        subcommand("struct") {
                            subcommand("test-find-unexplored") {
                                playerExecutes { context, player ->
                                    // This tests whether finding an unexplored structure
                                    // will let us find it with world.getStructures
                                    // and retrieves its bounding box and structure type.
                                    // answer: Yes, it does.

                                    val result = player.world.locateNearestStructure(
                                        player.location, Structure.SWAMP_HUT, 10000, true
                                    )

                                    if (result == null) {
                                        player.sendMessage("No structure found in the world.")
                                        return@playerExecutes
                                    }

                                    player.sendMessage("Located structure at: ${result.location}")

                                    val gotten = player.world.getStructures(result.location.chunk.x, result.location.chunk.z, Structure.SWAMP_HUT)

                                    if (gotten.isEmpty()) {
                                        player.sendMessage("No structures found in chunk ${player.location.chunk.x}, ${player.location.chunk.z}")
                                    } else {
                                        gotten.forEach { struct ->
                                            player.sendMessage("Got structure data: ${struct.structure.structureType} at ${struct.boundingBox}")
                                        }
                                    }
                                }
                            }
                        }
                        subcommand("map") {
                            subcommand("monument") {
                                playerExecutes { context, player ->
                                    createStructureMap(player.location, plugin, structure = Structure.MONUMENT, title = "Monument")?.let {
                                        player.give(it)
                                    }
                                }
                            }
                            subcommand("fortress") {
                                playerExecutes { context, player ->
                                    createNetherFortressMap(player.location, plugin)?.let {
                                        player.give(it)
                                    }
                                }
                            }
                            subcommand("hut") {
                                playerExecutes { context, player ->
                                    createStructureMap(player.location, plugin, structure = Structure.SWAMP_HUT, title = "Swamp Hut")?.let {
                                        player.give(it)
                                    }
                                }
                            }
                            subcommand("outpost") {
                                playerExecutes { context, player ->
                                    createStructureMap(player.location, plugin, structure = Structure.PILLAGER_OUTPOST, title = "Pillager Outpost")?.let {
                                        player.give(it)
                                    }
                                }
                            }
                        }
                        subcommand("explorer-map") {
                            playerExecutes { context, player ->
                                val result = player.world.locateNearestStructure(player.location, Structure.PILLAGER_OUTPOST, 1000, false)
                                player.sendMessage(result.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}

