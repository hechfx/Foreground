plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

tasks {
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val docsJar by registering(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Javadocs"
        archiveExtension.set("javadoc")
        from(javadoc)
        dependsOn(javadoc)
    }

    publishing {
        publications {
            create<MavenPublication>("Foreground") {
                groupId = Library.Group
                version = Library.Version

                from(components["java"])
                artifact(sourcesJar.get())
                artifact(docsJar.get())

                pom {
                    name.set("Foreground")
                    description.set("A powerful and simple-to-use BlueSky wrapper made in Kotlin. ")
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
                    maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
                        credentials {
                            username = System.getenv("GITHUB_USER")
                            password = System.getenv("GITHUB_TOKEN")
                        }
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["Foreground"])
}
