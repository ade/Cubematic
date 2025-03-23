package se.ade.mc.cubematic.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.*
import org.bukkit.plugin.Plugin
import java.io.File

private const val CONFIG_FILE = "cubematic-config.yml"
class CubeConfigProvider(private val plugin: Plugin) {
    private var currentConfig: CubeConfig? = null
    val current: CubeConfig
        get() {
            return currentConfig ?: kotlin.run {
                getConfig()?.also {
                    currentConfig = it
                } ?: CubeConfig()
            }
        }

    init {
        writeDefaultConfigIfDoesntExist()
    }

    private fun getConfig(): CubeConfig? {
        val f = File(plugin.dataFolder, CONFIG_FILE)
        if(!f.exists()) {
            return null
        }
        return try {
            val fileContents = f.readText()
            Yaml.default.decodeFromString(CubeConfig.serializer(), fileContents)
        } catch (e: Throwable) {
            null
        }
    }
    private fun writeDefaultConfigIfDoesntExist() {
        val f = File(plugin.dataFolder, CONFIG_FILE)
        if(f.exists()) {
            return
        }

        save(CubeConfig())
    }
    private fun save(config: CubeConfig) {
        plugin.dataFolder.mkdirs()
        File(plugin.dataFolder, CONFIG_FILE).writeText(Yaml.default.encodeToString(CubeConfig.serializer(), config))
    }
}
@Serializable
data class CubeConfig(
    @YamlComment("Debug mode")
    val debug: Boolean = false
)