package se.ade.mc.cubematic.extensions

import org.bukkit.block.Dropper
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

fun Dropper.getDropSlot(plugin: se.ade.mc.cubematic.CubematicPlugin) = this.persistentDataContainer.getDropSlot(plugin)
fun Dropper.setDropSlot(plugin: se.ade.mc.cubematic.CubematicPlugin, slot: Byte) {
    this.persistentDataContainer.setDropSlot(plugin, slot)
    this.update()
}
fun PersistentDataContainer.setDropSlot(plugin: se.ade.mc.cubematic.CubematicPlugin, slot: Byte) {
    set(plugin.namespaceKeys.dropSlot, PersistentDataType.BYTE, slot)
}

fun PersistentDataContainer.getDropSlot(plugin: se.ade.mc.cubematic.CubematicPlugin): Byte? {
    return get(plugin.namespaceKeys.dropSlot, PersistentDataType.BYTE)
}