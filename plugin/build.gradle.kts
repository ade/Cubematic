import java.util.Properties

plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.pluginYmlBukkit)
    alias(libs.plugins.runPaper)
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
    implementation(libs.paper)

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

    apiVersion = "1.21"
}

tasks {
    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("1.21.4")

        runDirectory.set(rootProject.layout.projectDirectory.dir(".servers/papermc"))
    }
}