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
    compileOnly(libs.papermc.api)
}

kotlin {
    jvmToolchain(21)
}