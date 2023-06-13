package se.ade.mc.cubematic.config

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.*
import java.io.File

private const val CONFIG_PATH = "plugins/cubematic"
private const val CONFIG_FILE = "$CONFIG_PATH/cubematic-config.yml"
object CubeConfigProvider {

    fun getConfig(): CubeConfig? {
        val f = File(CONFIG_FILE)
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
    fun writeDefaultConfigIfDoesntExist() {
        val f = File(CONFIG_FILE)
        if(f.exists()) {
            return
        }

        save(CubeConfig())
    }
    private fun save(config: CubeConfig) {
        File(CONFIG_PATH).mkdirs()
        File(CONFIG_FILE).writeText(Yaml.default.encodeToString(CubeConfig.serializer(), config))
    }
}
@Serializable
data class CubeConfig(
    @YamlComment("Debug mode")
    val debug: Boolean = false
)