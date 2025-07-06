package se.ade.mc.cubematic.hud

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

class HudUpdaterJob(private val hudPlugin: CubematicHudPlugin): Runnable {
	override fun run() {
		hudPlugin.server.onlinePlayers.forEach { player ->
			val component = Component.text("XYZ: ", NamedTextColor.GOLD)
				.append(Component.text(getCoordinates(player), NamedTextColor.WHITE))
				.append(Component.text(" "))
				.append(Component.text(getPlayerDirection(player), NamedTextColor.GOLD)
			)
			player.sendActionBar(component)
		}
	}

	private fun getCoordinates(p: Player): String {
		val loc = p.location
		return String.format("%s %s %s", loc.blockX, loc.blockY, loc.blockZ)
	}

	private fun getPlayerDirection(player: Player): String {
		//-180: Leaning left | +180: Leaning right
		var yaw = player.location.yaw
		//Bring to 360 degrees (Clockwise from -X axis)
		if (yaw < 0.0f) {
			yaw += 360.0f
		}
		//Separate into 8 sectors (Arc: 45deg), offset by 1/2 sector (Arc: 22.5deg)
		val sector = ((yaw + 22.5f) / 45f).toInt()
		return when (sector) {
			1 -> "SW/-X+Z"
			2 -> "W/-X"
			3 -> "NW/-XZ"
			4 -> "N/-Z"
			5 -> "NE/+X-Z"
			6 -> "E/+X"
			7 -> "SE/+XZ"
			0 -> "S/+Z"
			else -> "S/+Z"
		}
	}
}