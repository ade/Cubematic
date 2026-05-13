package se.ade.mc.cubematic.agent.ui

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import se.ade.mc.cubematic.core.agent.config.InferenceConfig
import se.ade.mc.cubematic.core.agent.config.InferenceProviderConfig
import java.io.File

@Serializable
data class AgentUiConfig(
	val inferenceConfig: InferenceConfig = InferenceConfig(
		providers = listOf(
			InferenceProviderConfig.OpenAIInferenceConfig(
				apiKey = "",
				baseUrl = "https://127.0.0.1:10000"
			)
		)
	)
)

fun loadConfig(): AgentUiConfig {
	val file = File("ui-config.json")

	val json = Json {
		prettyPrint = true
		encodeDefaults = true
	}

	val content = if(file.exists()) {
		file.readText()
	} else {
		val defaultConfig = AgentUiConfig()
		val str = json.encodeToString(defaultConfig)
		file.writeText(str)
		str
	}
	val result = json.decodeFromString<AgentUiConfig>(content)

	if(result.inferenceConfig.providers.isEmpty())
		throw Error("Providers in config cannot be empty. Please add at least one provider to the config file.")

	return result
}