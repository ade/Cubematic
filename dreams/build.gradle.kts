import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
	kotlin("jvm")
	alias(libs.plugins.shadow)
	alias(libs.plugins.pluginYmlBukkit)
	alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.dreams"

repositories {
	mavenCentral()
}

dependencies {
	compileOnly(kotlin("stdlib"))
	compileOnly(libs.paper)
	compileOnly(libs.multiverse2)
	compileOnly(libs.sqlite)
	compileOnly(libs.bundles.exposed)
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
	archiveBaseName.set("cubematic-dreams")
}

bukkit {
	main = "se.ade.mc.cubematic.dreams.CubematicDreamsPlugin"
	name = "Cubematic-Dreams"
	load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

	author = "ade"
	description = "tbd"
	version = project.version.toString()

	apiVersion = "1.21"
	depend = listOf("Cubematic-Runtime")

	permissions {
		register("skyblock.command") {
			description = "Allows the player to use the skyblock command"
			default = BukkitPluginDescription.Permission.Default.OP
		}
	}

	commands {
		register("dream") {
			description = "Dreamcommand"
			permission = "skyblock.command"
		}
	}
}