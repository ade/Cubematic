package se.ade.mc.cubematic.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.bukkit.plugin.Plugin
import java.io.File
import kotlin.reflect.KProperty

class ConfigProvider<T : Any>(
	private val plugin: Plugin,
	private val fileName: String,
	private val serializer: KSerializer<T>,
	private val defaultConfig: () -> T
) {
	private var currentConfig: T? = null
	val current: T
		get() {
			return currentConfig ?: kotlin.run {
				getConfig()?.also {
					currentConfig = it
				} ?: defaultConfig().also { save(it) }
			}
		}

	init {
		writeDefaultConfigIfDoesntExist()
	}

	private fun getConfig(): T? {
		val f = File(plugin.dataFolder, fileName)
		if(!f.exists()) {
			return null
		}
		return try {
			val fileContents = f.readText()
			Yaml.default.decodeFromString(serializer, fileContents)
		} catch (e: Throwable) {
			plugin.logger.warning("Failed to load config $fileName: ${e.message}")
			null
		}
	}

	private fun writeDefaultConfigIfDoesntExist() {
		val f = File(plugin.dataFolder, fileName)
		if(f.exists()) {
			return
		}
		save(defaultConfig())
	}

	fun save(config: T) {
		plugin.dataFolder.mkdirs()
		File(plugin.dataFolder, fileName).writeText(Yaml.default.encodeToString(serializer, config))
		currentConfig = config
	}

	// Property delegation support
	operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return current
	}

	// Mutable property delegation support
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		save(value)
	}
}

/**
 * Creates a config provider for a serializable class
 */
inline fun <reified T : Any> Plugin.configProvider(
	fileName: String = "config.yml",
	noinline defaultConfig: () -> T
): ConfigProvider<T> {
	return ConfigProvider(this, fileName, serializer<T>(), defaultConfig)
}
