
rootProject.name = "cubematic"

include(
    "core",
    "plugin",
    "paper",
    "utils"
)

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.22")

            plugin("kotlinx-serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")

            library("kaml", "com.charleskorn.kaml", "kaml").version("0.54.0")
        }
    }
}