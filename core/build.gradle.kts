plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.cbor)
    implementation(libs.caffeine)
    implementation(libs.commons.codec)
}