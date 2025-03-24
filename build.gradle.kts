plugins {
    kotlin("jvm") version libs.versions.kotlin apply false
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.onarandombox.com/content/groups/public/")
    }
}