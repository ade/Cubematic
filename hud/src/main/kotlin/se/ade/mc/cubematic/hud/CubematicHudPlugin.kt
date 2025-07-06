package se.ade.mc.cubematic.hud

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask

class CubematicHudPlugin: JavaPlugin() {
	var job: BukkitTask? = null

	override fun onEnable() {
		logger.info("Cubematic Hud Plugin enabled")
		job?.cancel()
		job = server.scheduler.runTaskTimer(this, HudUpdaterJob(this), 0L, 10L)
	}

	override fun onDisable() {
		job?.cancel()
		logger.info("Cubematic Hud Plugin disabled")
	}
}