plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.cbor)
    implementation(libs.caffeine)
    implementation(libs.commons.codec)
}

publishing {
    publications {
        register("PerfectDreams", MavenPublication::class.java) {
            from(components["java"])
        }
    }
}
