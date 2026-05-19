plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.library) apply false
}

buildscript {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

allprojects {
    repositories {
	    mavenLocal()
	    mavenCentral()
	    google()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.onarandombox.com/content/groups/public/")
    }
}

task("jars") {
	val features = listOf("agent:agent-plugin", "automation", "dreams", "hud", "inthesky", "portals", "runtime")
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