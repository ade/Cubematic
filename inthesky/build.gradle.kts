import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginYmlBukkit)
    alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.inthesky"

repositories {
    mavenCentral()
}

dependencies {
	compileOnly(kotlin("stdlib"))
    compileOnly(libs.paper)
    compileOnly(libs.kaml)
	compileOnly(libs.kotlinx.coroutines.core)
	compileOnly(libs.sqlite)
	compileOnly(libs.bundles.exposed)

	implementation(project(":core:plugin-core"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(25)
}

tasks.shadowJar {
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
    archiveBaseName.set("cubematic-inthesky")
}

tasks.register<Copy>("deploy") {
    //Check out local.properties.example
    val deployPath = Properties().let {
        it.load(project.rootProject.file("local.properties").inputStream())
        it.getProperty("deployTo", "./build")
    }

    dependsOn(tasks.getByName("shadowJar"))
    from(layout.buildDirectory.dir("libs/"))
    into(deployPath)
    include("*plugin*$version*all.jar")
}

bukkit {
    main = "se.ade.mc.skyblock.CubematicSkyPlugin"
    name = "Cubematic-InTheSky"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    author = "ade"
    description = "tbd"
    version = project.version.toString()

    apiVersion = "26.1"

	depend = listOf("Cubematic-Runtime")

    permissions {
        register("cubematic.inthesky.commands") {
            description = "Allows cubematic inthesky commands"
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }

    commands {
        register("cubematic") {
            description = "Cubematic inthesky"
            permission = "skyblock.command"
        }
    }
}