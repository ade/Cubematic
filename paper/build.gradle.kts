import java.util.Properties

plugins {
    kotlin("jvm")
    `java-library`
}

repositories {
    mavenCentral()
    maven(url="https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url="https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":core"))
    testImplementation(kotlin("test"))
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")
}

kotlin {
    jvmToolchain(17)
}