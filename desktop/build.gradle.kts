import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.foss"
version = "1.0.0"

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.gson)
    implementation(libs.jetbrains.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Main-Class" to "com.foss.aihub.pc.MainKt"
        )
    }
}

tasks.shadowJar {
    manifest {
        attributes(
            "Main-Class" to "com.foss.aihub.pc.MainKt"
        )
    }
    archiveClassifier.set("")
}