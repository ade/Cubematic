package se.ade.mc.skyblock.nether

import se.ade.mc.skyblock.CubeInTheSkyPlugin

class NetherFacet(private val plugin: CubeInTheSkyPlugin) {
	fun onEnable() {
		plugin.server.pluginManager.registerEvents(NetherListener(plugin), plugin)
	}

	fun onDisable() {
		// Nothing to do
	}
}