import java.util.Properties
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    kotlin("jvm") version "2.1.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
}

group = "se.ade.mc.cubematic.inthesky"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.paper)
    implementation(libs.sqlite)
    implementation(libs.bundles.exposed)

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
    main = "se.ade.mc.skyblock.AdeSkyblockPlugin"
    name = "Cubematic-InTheSky"
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP

    author = "ade"
    description = "tbd"
    version = project.version.toString()

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

    apiVersion = "1.21"
}