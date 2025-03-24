package se.ade.mc.cubematic.dreams

import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.extensions.commands

class CubematicDreamsPlugin: JavaPlugin() {
	val facet = DreamFacet(this)

	override fun onEnable() {
		facet.onEnable()
		commands {
			command("dream") {
				subcommand("create") {
					playerExecutes { _, player ->
						facet.onCreateDreamWorldCommand()
					}
				}
				subcommand("enter") {
					playerExecutes { _, player ->
						facet.onEnterDreamWorldCommand(player)
					}
				}
				subcommand("leave") {
					playerExecutes { _, player ->
						facet.onLeaveDreamWorldCommand(player)
					}
				}
				subcommand("destroy") {
					playerExecutes { _, player ->
						facet.onDestroyDreamWorldCommand()
					}
				}
				subcommand("inventory") {
					subcommand("stash") {
						playerExecutes { _, player ->
							facet.onStashInventoryCommand(player)
						}
					}
					subcommand("pop") {
						playerExecutes { _, player ->
							facet.onPopInventoryCommand(player)
						}
					}
				}
			}
		}
	}

	override fun onDisable() {
		facet.onDisable()
	}
}