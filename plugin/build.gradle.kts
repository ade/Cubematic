import java.util.Properties

plugins {
    kotlin("jvm")
    `java-library`
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    alias(libs.plugins.kotlinx.serialization)
}

group = "se.ade.mc.cubematic"

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url="https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":core"))
    api(project(":utils"))

    implementation(libs.kaml)
    compileOnly(libs.papermc.api)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveBaseName.set("cubematic")
    }
}
kotlin {
    jvmToolchain(21)
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
    include("*cubematic*$version*all.jar")
}

bukkit {
    main = "se.ade.mc.cubematic.CubematicPlugin"
    name = "Cubematic"
    load = net.minecrell.pluginyml.bukkit.BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    author = "ade"
    description = "auto-crafting, block-placing, block-breaking, teleportation"
    version = project.version.toString()

    apiVersion = "1.19"
}