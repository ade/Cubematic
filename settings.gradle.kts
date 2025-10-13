rootProject.name = "Cubematic"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "core",
	"agent:agent-core",
	"agent:agent-plugin",
	"agent:agent-ui",
    "automation",
    "dreams",
    "hud",
    "inthesky",
    "portals",
	"runtime"
)