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

task("jars") {
    dependsOn(":dreams:shadowJar")
    dependsOn(":hud:shadowJar")
    dependsOn(":inthesky:shadowJar")
    dependsOn(":portals:shadowJar")
    val targetPath = rootProject.projectDir.resolve("build/prod")
    doLast {
        val jars = listOf(
            project(":automation").tasks.named("shadowJar").get().outputs.files.first(),
            project(":hud").tasks.named("shadowJar").get().outputs.files.first(),
            project(":inthesky").tasks.named("shadowJar").get().outputs.files.first(),
            project(":portals").tasks.named("shadowJar").get().outputs.files.first(),
        )

        targetPath.mkdirs()
        jars.forEach { jar ->
            copy {
                from(jar)
                into(targetPath)
            }
        }
    }
}