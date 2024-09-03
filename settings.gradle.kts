plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "foreground"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("foreground.versions.toml"))
        }
    }
}

include("foreground-core")
include("example-bot")

