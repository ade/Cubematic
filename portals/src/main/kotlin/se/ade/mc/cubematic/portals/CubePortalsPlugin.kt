package se.ade.mc.cubematic.portals

import org.bukkit.plugin.java.JavaPlugin

class CubePortalsPlugin: JavaPlugin() {
	private val debug = true

	override fun onEnable() {
		server.pluginManager.registerEvents(PortalAspect(this, debug), this)
	}
}