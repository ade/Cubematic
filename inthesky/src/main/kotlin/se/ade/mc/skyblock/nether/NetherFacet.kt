package se.ade.mc.skyblock.nether

import se.ade.mc.skyblock.CubematicSkyPlugin

class NetherFacet(private val plugin: CubematicSkyPlugin) {
	fun onEnable() {
		plugin.server.pluginManager.registerEvents(NetherListener(plugin), plugin)
	}

	fun onDisable() {
		// Nothing to do
	}
}