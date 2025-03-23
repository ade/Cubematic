import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "2.1.10"
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginYmlBukkit)
    alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic.portals"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.paper)
    implementation(libs.sqlite)
    implementation(libs.kaml)

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
}

bukkit {
    main = "se.ade.mc.cubematic.portals.CubematicPortalsPlugin"
    name = "Cubematic-Portals"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    author = "ade"
    description = "Portals plugin"
    version = project.version.toString()

    apiVersion = "1.21"
}