rootProject.name = "Cubematic"

pluginManagement {
	repositories {
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		google()
		gradlePluginPortal()
		mavenCentral()
	}

	resolutionStrategy {
		eachPlugin {
			if (listOf("com.android.application", "com.android.library").any { requested.id.id == it.lowercase() }) {
				useModule("com.android.tools.build:gradle:${requested.version}")
			}
		}
	}
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "core:agent-core",
    "core:plugin-core",
    "core:wiki-parser",
	"agent:agent-plugin",
	"agent:agent-ui",
    "automation",
    "dreams",
    "hud",
    "inthesky",
    "portals",
	"runtime"
)