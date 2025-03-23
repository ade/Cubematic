package se.ade.mc.cubematic.portals

import org.bukkit.GameEvent
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.SculkShrieker
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.GenericGameEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ShriekerTest(private val plugin: CubePortalsPlugin): Listener {
    @EventHandler
    fun onEvent(e: GenericGameEvent) {
        if(e.event != GameEvent.SHRIEK)
            return

        val shrieker = e.location.block.blockData as? SculkShrieker
            ?: return


        plugin.server.broadcastMessage(shrieker.toString())

        val player = e.entity as? Player
        plugin.server.broadcastMessage(player.toString())

        player?.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 60, 0, false, true, false))

        e.location.block.getRelative(BlockFace.UP).type = Material.END_GATEWAY
    }
}