plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.shadow) apply false
    `maven-publish`
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    apply(plugin = "maven-publish")

    group = "me.hechfx"
    version = "0.0.1"

    dependencies {
        implementation(kotlin("stdlib"))
    }

    if (project.name == "core") {
        publishing {
            publications {
                create<MavenPublication>("gpr") {
                    from(components["kotlin"])

                    pom {
                        name.set(project.name)
                        description.set("Core module for Foreground (BlueSky's API Wrapper)")
                        url.set("https://github.com/hechfx/Foreground")

                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }

                        developers {
                            developer {
                                id.set("hechfx")
                                name.set("André victor")
                            }
                        }
                        
                        scm {
                            connection.set("scm:git:git://github.com/hechfx/Foreground.git")
                            developerConnection.set("scm:git:git://github.com/hechfx/Foreground.git")
                            url.set("scm:git:git://github.com/hechfx/Foreground")
                        }
                    }
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
