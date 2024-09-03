
rootProject.name = "Cubematic"

include(
    "core",
    "plugin",
    "utils"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.0.20")

            plugin("kotlinx-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            library("kaml", "com.charleskorn.kaml", "kaml").version("0.54.0")
            library("papermc-api", "io.papermc.paper", "paper-api").version("1.21.1-R0.1-SNAPSHOT")
        }
    }
}