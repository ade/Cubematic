package se.ade.mc.cubematic.agent.ui

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class AgentUiConfig(
	val apiKey: String = "",
	val baseUrl: String = ""
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

	if(result.baseUrl.isEmpty())
		throw Error("Base URL in config cannot be empty")

	return result
}