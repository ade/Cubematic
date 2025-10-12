import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	kotlin("jvm")
	alias(libs.plugins.shadow)
	alias(libs.plugins.pluginYmlBukkit)
	alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.hud"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("stdlib"))
	compileOnly(libs.paper)
	compileOnly(libs.kaml)

	implementation(project(":core"))
	testImplementation(kotlin("test"))
}

tasks.test {
	useJUnitPlatform()
}
kotlin {
	jvmToolchain(21)
}

tasks.shadowJar {
	manifest {
		attributes["paperweight-mappings-namespace"] = "mojang"
	}
	archiveBaseName.set("cubematic-hud")
}

bukkit {
	main = "se.ade.mc.cubematic.hud.CubematicHudPlugin"
	name = "Cubematic-Hud"
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

	author = "ade"
	description = "tbd"
	version = project.version.toString()

	apiVersion = "1.21"

	depend = listOf("Cubematic-Runtime")
}