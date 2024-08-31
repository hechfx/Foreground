plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "me.hechfx"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.oshai:kotlin-logging-jvm:4.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.3")

    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-hocon:1.3.0")
    implementation("commons-codec:commons-codec:1.15")

    // https://mvnrepository.com/artifact/com.upokecenter/cbor
    implementation("com.upokecenter:cbor:5.0.0-alpha2")

    // cache & stuff
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
