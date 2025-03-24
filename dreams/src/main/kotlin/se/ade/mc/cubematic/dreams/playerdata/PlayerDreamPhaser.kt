package se.ade.mc.cubematic.dreams.playerdata

import com.onarandombox.MultiverseCore.MultiverseCore
import org.bukkit.World
import org.bukkit.entity.Player
import se.ade.mc.cubematic.dreams.PlayerStatus
import se.ade.mc.cubematic.dreams.datastore.SkyDb
import se.ade.mc.cubematic.dreams.inventory.PlayerDreamInventories
import java.util.logging.Logger

class PlayerDreamPhaser(
	private val homeWorld: World,
	private val inventories: PlayerDreamInventories,
	private val db: SkyDb,
	private val logger: Logger,
	private val multiverse: MultiverseCore
) {
	fun begin(player: Player, dreamWorld: World) {
		db.playerLocationStash(
			uuid = player.identity().uuid(),
			location = player.location
		)

		inventories.stash(player)

		db.playerStatusStash(
			uuid = player.identity().uuid(),
			status = PlayerStatus(
				health = player.health,
				food = player.foodLevel,
				air = player.remainingAir
			)
		)

		multiverse.teleportPlayer(player, player, dreamWorld.getBlockAt(8,100,8).location)
		//player.teleport(dreamWorld.spawnLocation)
	}

	fun end(player: Player) {
		val destination = db.playerLocationPop(player.identity().uuid(), homeWorld)
			?: homeWorld.spawnLocation

		logger.info { "Teleport: $destination" }
		player.teleport(destination)
		inventories.pop(player)

		player.fireTicks = 0
		player.fallDistance = 0f

		player.activePotionEffects.forEach {
			player.removePotionEffect(it.type)
		}

		db.playerStatusPop(player.identity().uuid())?.let {
			player.health = it.health
			player.foodLevel = it.food
			player.remainingAir = it.air
			player.sendHealthUpdate()
		}

		player.velocity = player.velocity.zero()
	}
}