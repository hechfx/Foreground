plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow) apply true
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":foreground-core"))

    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.hocon)
}

tasks {
    shadowJar {
        archiveBaseName.set("example-bot")
        archiveClassifier.set("")
        archiveVersion.set("")

        manifest {
            attributes["Main-Class"] = "me.hechfx.bot.ExampleBotLauncher"
        }

        from(project(":foreground-core").sourceSets["main"].output)
        mergeServiceFiles()
    }
}
