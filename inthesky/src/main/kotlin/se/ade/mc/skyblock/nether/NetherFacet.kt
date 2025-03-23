package se.ade.mc.skyblock.nether

import se.ade.mc.skyblock.AdeSkyblockPlugin

class NetherFacet(private val plugin: AdeSkyblockPlugin) {
	fun onEnable() {
		plugin.server.pluginManager.registerEvents(NetherListener(plugin), plugin)
	}

	fun onDisable() {
		// Nothing to do
	}
}