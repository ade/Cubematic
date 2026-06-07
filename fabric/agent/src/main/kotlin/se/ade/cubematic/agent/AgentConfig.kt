package se.ade.cubematic.agent

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import se.ade.mc.cubematic.core.agent.config.InferenceConfig
import se.ade.mc.cubematic.core.agent.config.InferenceModelConfig
import se.ade.mc.cubematic.core.agent.config.InferenceProviderConfig
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class AgentConfig(
	val apiKey: String = "",
	val baseUrl: String = "",
	val inferenceConfig: InferenceConfig = InferenceConfig(
		providers = listOf(
			InferenceProviderConfig.OpenAIInferenceConfig(
				apiKey = "example",
				baseUrl = "example",
				model = InferenceModelConfig(
					id = "default",
					contextLength = 128_000,
				)
			)
		)
	),
) {
	companion object {
		private val logger = LoggerFactory.getLogger("cubematic-agent")
		private const val FILE_NAME = "cubematic-agent.yaml"

		/**
		 * Loads the agent config from the Fabric config directory, writing a default file if missing.
		 */
		fun load(): AgentConfig {
			val path = FabricLoader.getInstance().configDir.resolve(FILE_NAME)

			if (!path.exists()) {
				val default = AgentConfig()
				runCatching {
					path.writeText(Yaml.default.encodeToString(serializer(), default))
				}.onFailure { logger.warn("Failed to write default config $FILE_NAME: ${it.message}") }
				return default
			}

			return runCatching {
				Yaml.default.decodeFromString(serializer(), path.readText())
			}.getOrElse {
				logger.warn("Failed to load config $FILE_NAME, using defaults: ${it.message}")
				AgentConfig()
			}
		}
	}
}

