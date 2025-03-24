package se.ade.mc.cubematic.portals

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import se.ade.mc.cubematic.config.configProvider
import se.ade.mc.cubematic.extensions.commands

class CubematicPortalsPlugin: JavaPlugin() {
	private val config by configProvider { PortalsConfig() }

	override fun onEnable() {
		server.pluginManager.registerEvents(PortalAspect(this, config.debug), this)

		if(config.debug) {
			addCommands()
		}
	}

	private fun addCommands() {
		commands {
			command("cubematic") {
				subcommand("portals") {
					subcommand("debug") {
						subcommand("give-compass") {
							playerExecutes { context, player ->
								val item = ItemStack(Material.COMPASS).also {
									it.addEnchantment(Enchantment.VANISHING_CURSE, 1)
								}
								player.inventory.addItem(item)
							}
						}
					}
				}
			}
		}
	}
}