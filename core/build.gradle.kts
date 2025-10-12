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
	compileOnly(libs.paper)
	compileOnly(kotlin("stdlib"))
	compileOnly(libs.kaml)
	compileOnly(libs.kotlinx.coroutines.core)
}

kotlin {
    jvmToolchain(21)
}