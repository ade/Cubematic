rootProject.name = "Cubematic"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(
    "core",
    "automation",
    "dreams",
    "hud",
    "inthesky",
    "portals",
)