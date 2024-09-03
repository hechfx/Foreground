
plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

allprojects {
    group = Library.Group
    version = Library.Version

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply<MavenPublishPlugin>()

    if (project.name != "example-bot") {
        version = Library.Version

        publishing {
            repositories {
                maven {
                    name = "PerfectDreams"
                    url = uri("https://repo.perfectdreams.net/")
                    credentials {
                        username = System.getenv("PERFECTDREAMS_USERNAME") ?: ""
                        password = System.getenv("PERFECTDREAMS_PASSWORD") ?: ""
                    }
                }
            }
        }
    }
}
