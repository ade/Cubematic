package se.ade.mc.skyblock.datastore

import org.bukkit.Location
import org.bukkit.World
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.deleteReturning
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.replace
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import se.ade.mc.skyblock.CubematicSkyPlugin
import se.ade.mc.skyblock.dream.PlayerStatus
import se.ade.mc.skyblock.dream.inventory.PlayerHibernation
import java.io.File
import java.sql.Connection
import java.util.UUID

class SkyDb(private val plugin: CubematicSkyPlugin) {

	private val dbFile = File(plugin.dataFolder, "skyblock.db").absolutePath

	val db by lazy {
		plugin.dataFolder.mkdirs()
		TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
		Database.connect("jdbc:sqlite:$dbFile", "org.sqlite.JDBC")
	}

	init {
		tx {
			if(!PlayerInventoryTable.exists()) {
				create(PlayerInventoryTable)
			}
			if(!PlayerLocationTable.exists()) {
				create(PlayerLocationTable)
			}
			if(!PlayerStatusTable.exists()) {
				create(PlayerStatusTable)
			}
		}
	}

	fun playerInventoryStash(uuid: UUID, inventory: ByteArray, armor: ByteArray) {
		tx {
			PlayerInventoryTable.replace {
				it[PlayerInventoryTable.uuid] = uuid.toString()
				it[PlayerInventoryTable.inventory] = inventory
				it[PlayerInventoryTable.armor] = armor
			}
		}
	}

	fun playerInventoryPop(uuid: UUID): PlayerHibernation? {
		return tx {
			PlayerInventoryTable.deleteReturning {
				PlayerInventoryTable.uuid eq uuid.toString()
			}
			.mapNotNull {
				PlayerHibernation(
					uuid = it[PlayerInventoryTable.uuid],
					inventory = it[PlayerInventoryTable.inventory],
					armor = it[PlayerInventoryTable.armor]
				)
			}
			.singleOrNull()
		}
	}

	fun playerLocationStash(uuid: UUID, location: Location) {
		tx {
			PlayerLocationTable.replace {
				it[PlayerLocationTable.uuid] = uuid.toString()
				it[x] = location.x
				it[y] = location.y
				it[z] = location.z
				it[yaw] = location.yaw
				it[pitch] = location.pitch
			}
		}
	}

	fun playerLocationPop(uuid: UUID, world: World): Location? {
		return tx {
			PlayerLocationTable.deleteReturning {
				PlayerLocationTable.uuid eq uuid.toString()
			}
			.mapNotNull {
				val x = it[PlayerLocationTable.x]
				val y = it[PlayerLocationTable.y]
				val z = it[PlayerLocationTable.z]
				val yaw = it[PlayerLocationTable.yaw]
				val pitch = it[PlayerLocationTable.pitch]

				Location(world, x, y, z, yaw, pitch)
			}
			.singleOrNull()
		}
	}

	fun playerStatusStash(uuid: UUID, status: PlayerStatus) {
		tx {
			PlayerStatusTable.replace {
				it[PlayerStatusTable.uuid] = uuid.toString()
				it[PlayerStatusTable.health] = status.health
				it[PlayerStatusTable.food] = status.food
				it[PlayerStatusTable.air] = status.air
			}
		}
	}

	fun playerStatusPop(uuid: UUID): PlayerStatus? {
		return tx {
			PlayerStatusTable.deleteReturning {
				PlayerStatusTable.uuid eq uuid.toString()
			}
			.mapNotNull {
				PlayerStatus(
					health = it[PlayerStatusTable.health],
					food = it[PlayerStatusTable.food],
					air = it[PlayerStatusTable.air]
				)
			}
			.singleOrNull()
		}
	}

	private fun <T> tx(statement: Transaction.() -> T) = transaction(db, statement)
}

