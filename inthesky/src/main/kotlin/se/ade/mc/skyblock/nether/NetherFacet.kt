package se.ade.mc.skyblock.nether

import se.ade.mc.cubematic.extensions.Aspect
import se.ade.mc.skyblock.CubematicSkyPlugin

class NetherFacet(private val plugin: CubematicSkyPlugin): Aspect(plugin) {

	override fun enable() {
		addListener(NetherListener(plugin))
	}

	override fun disable() {
		// Nothing to do
	}
}