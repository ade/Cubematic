import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.gradle.kotlin.dsl.invoke

plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginYmlBukkit)
    alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.inthesky"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":core"))
    implementation(libs.kaml)
    implementation(libs.kotlinx.coroutines.core)

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

    apiVersion = "1.21"
}