plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

tasks {
    publishing {
        publications {
            register<MavenPublication>("gpr") {
                groupId = Library.Group
                version = Library.Version

                from(components["java"])

                pom {
                    name.set("Foreground")
                    description.set("A simple-to-use BlueSky wrapper made in Kotlin. ")
                    url.set("https://github.com/hechfx/Foreground")

                    developers {
                        developer {
                            name.set("hechfx")
                            email.set("hechfx@hotmail.com")
                        }
                    }

                    licenses {
                        license {
                            name.set("MIT")
                            url.set("https://opensource.org/licenses/mit-license.php")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/hechfx/Foreground.git")
                        developerConnection.set("scm:git:ssh://github.com/hechfx/Foreground.git")
                        url.set("https://github.com/hechfx/Foreground")
                    }
                }

                repositories {
                    maven {
                        name = "GitHubPackages"
                        url = uri("https://maven.pkg.github.com/hechfx/Foreground")

                        credentials {
                            username = System.getenv("GITHUB_USERNAME")
                            password = System.getenv("GITHUB_TOKEN")
                        }
                    }
                }
            }
        }
    }
}
