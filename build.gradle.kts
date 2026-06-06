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

	///// BEGIN FIX - AGP bundled ASM version clashes with Loom /////
	// Because we need a version of ASM that can handle Java 25
	configurations.classpath {
		resolutionStrategy {
			force(
				"org.ow2.asm:asm:9.8",
				"org.ow2.asm:asm-commons:9.8",
				"org.ow2.asm:asm-tree:9.8",
				"org.ow2.asm:asm-analysis:9.8",
				"org.ow2.asm:asm-util:9.8",
			)
		}
	}
	///// END FIX - AGP bundled ASM version clashes with Loom /////
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