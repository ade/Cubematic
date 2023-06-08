package se.ade.mc.cubematic.extensions

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin

open class Aspect(val plugin: Plugin): Listener
inline fun <reified T: Event> Aspect.listenTo(noinline handler: (T) -> Unit) {
    this.plugin.server.pluginManager.registerEvent(
        T::class.java,
        this,
        EventPriority.NORMAL,
        { _, event -> handler(event as T) },
        this.plugin
    )
}