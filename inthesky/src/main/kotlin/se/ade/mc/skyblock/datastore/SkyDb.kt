package se.ade.mc.skyblock.datastore

import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils.create
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.exists
import org.jetbrains.exposed.v1.jdbc.replace
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import se.ade.mc.skyblock.structuremaps.StoredStructureMapData
import java.io.File
import java.sql.Connection

class SkyDb(private val plugin: JavaPlugin) {
	private val dbFile = File(plugin.dataFolder, "sky-db.sqlite").absolutePath

	val db by lazy {
		plugin.dataFolder.mkdirs()
		TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
		Database.connect("jdbc:sqlite:$dbFile", "org.sqlite.JDBC")
	}

	init {
		tx {
			if(!StructureMapTable.exists()) {
				create(StructureMapTable)
			}
		}
	}

	fun structureMapSave(mapId: Int, x: Double, z: Double, structureType: String) {
		tx {
			StructureMapTable.replace {
				it[StructureMapTable.mapId] = mapId
				it[StructureMapTable.structureType] = structureType
			}
		}
	}

	fun structureMapLoad(mapId: Int): StoredStructureMapData? {
		return tx {
			StructureMapTable
				.selectAll()
				.where {
					StructureMapTable.mapId eq mapId
				}.map {
					StoredStructureMapData(
						mapId = it[StructureMapTable.mapId],
						structureTypeKey = it[StructureMapTable.structureType]
					)
				}.singleOrNull()
		}
	}

	private fun <T> tx(statement: JdbcTransaction.() -> T) = transaction(db, statement = statement)
}

