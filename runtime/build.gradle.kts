import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.kotlin.dsl.invoke

plugins {
	kotlin("jvm")
	alias(libs.plugins.shadow)
	alias(libs.plugins.pluginYmlBukkit)
	alias(libs.plugins.kotlinx.serialization)
	alias(libs.plugins.runPaper)
}

group = "se.ade.mc.cubematic.runtime"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(libs.paper)

	implementation(project(":core:agent-core"))
	implementation(project(":core:plugin-core"))
	implementation(libs.kaml)
	implementation(libs.sqlite)
	implementation(libs.bundles.exposed)

	implementation(libs.koog)
	implementation(libs.kotlinx.coroutines.core)
	implementation(libs.kotlinx.datetime)
	implementation(libs.ktor.client.core)
	implementation(libs.ktor.client.content.negotiation)
	implementation(libs.ktor.serialization.kotlinx.json)
	implementation(libs.se.ade.kuri.api)

	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(21)
}

tasks {
	shadowJar {
		manifest {
			attributes["paperweight-mappings-namespace"] = "mojang"
		}
		archiveBaseName.set("cubematic-runtime")
	}
	runServer {
		val features = listOf("agent:agent-plugin", "runtime", "hud", "inthesky", "portals") //, "dreams")
		// Configure the Minecraft version for our task.
		// This is the only required configuration besides applying the plugin.
		// Your plugin's jar (or shadowJar if present) will be used automatically.
		minecraftVersion(libs.versions.minecraft.get().toString())
		features.forEach {
			dependsOn(":$it:shadowJar")

			// Add other plugin jars to the server
			pluginJars(project(":$it").tasks.named("shadowJar").get().outputs.files.first())
		}

		runDirectory.set(rootProject.layout.projectDirectory.dir(".servers/papermc"))
	}
}

bukkit {
	main = "se.ade.mc.CubematicRuntimePlugin"
	name = "Cubematic-Runtime"
	load = BukkitPluginDescription.PluginLoadOrder.STARTUP

	author = "ade"
	description = "tbd"
	version = project.version.toString()

	apiVersion = "1.21"
}