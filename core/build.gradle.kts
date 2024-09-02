plugins {
    `foreground-module-publishing`
}

repositories {
    mavenCentral()
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
