import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	kotlin("jvm")
	alias(libs.plugins.shadow)
	alias(libs.plugins.pluginYmlBukkit)
	alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.agent"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("stdlib"))
	compileOnly(libs.paper)
	compileOnly(libs.kaml)

	compileOnly(libs.koog)
	compileOnly(libs.kotlinx.coroutines.core)
	compileOnly(libs.ktor.client.core)
	compileOnly(libs.ktor.client.content.negotiation)
	compileOnly(libs.ktor.serialization.kotlinx.json)
	compileOnly(libs.se.ade.kuri.api)

	implementation(project(":core"))
	implementation(project(":agent:agent-core"))
}

kotlin {
	jvmToolchain(21)
}

tasks.shadowJar {
	manifest {
		attributes["paperweight-mappings-namespace"] = "mojang"
	}
	archiveBaseName.set("cubematic-agent")
}

bukkit {
	main = "se.ade.mc.cubematic.agent.CubematicAgentPlugin"
	name = "Cubematic-Agent"
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

	author = "ade"
	description = "tbd"
	version = project.version.toString()

	apiVersion = "1.21"

	depend = listOf("Cubematic-Runtime")

	permissions {
		register("cubematic.agent.command") {
			description = "Allows the player to use the agent commands"
			default = BukkitPluginDescription.Permission.Default.TRUE
		}
	}

	commands {
		register("ca") {
			description = "Cubematic AI agent command"
			permission = "cubematic.agent.command"
		}
	}
}