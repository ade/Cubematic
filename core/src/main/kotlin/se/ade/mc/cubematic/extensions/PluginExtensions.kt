package se.ade.mc.cubematic.extensions

import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

abstract class Aspect(protected val javaPlugin: JavaPlugin) {
    abstract fun enable()
    abstract fun disable()

    fun addListener(listener: Listener) {
        javaPlugin.server.pluginManager.registerEvents(listener, javaPlugin)
    }

    fun scheduleRun(delayTicks: Long = 0L, block: () -> Unit)
        = javaPlugin.scheduleRun(delayTicks, block)
}

fun JavaPlugin.scheduleRun(delayTicks: Long = 0L, block: () -> Unit) {
    server.scheduler.runTaskLater(this, Runnable { block() }, delayTicks)
}