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
	    google()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.onarandombox.com/content/groups/public/")
    }
}

task("jars") {
	val features = listOf("automation", "dreams", "hud", "inthesky", "portals", "runtime")
	features.forEach {
		dependsOn("$it:shadowJar")
	}

    val targetPath = rootProject.projectDir.resolve("build/prod")
    doLast {
		val jars = features.map {
			project(":$it").tasks.named("shadowJar").get().outputs.files.first()
		}

        targetPath.mkdirs()
        jars.forEach { jar ->
            copy {
                from(jar)
                into(targetPath)
            }
        }
    }
}