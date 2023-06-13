import java.util.Properties

plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":paper"))
    testImplementation(kotlin("test"))
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}