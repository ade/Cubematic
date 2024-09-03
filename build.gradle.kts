plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
}

buildscript {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}