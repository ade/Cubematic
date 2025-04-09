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
    implementation(libs.paper)
    implementation(libs.kaml)
    implementation(libs.kotlinx.coroutines.core)
}

kotlin {
    jvmToolchain(21)
}