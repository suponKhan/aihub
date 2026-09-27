import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

group = "com.foss"
version = "1.0.0"

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

compose.desktop {
    application {
        mainClass = "com.foss.aihub.pc.MainKt"
        nativeDistributions {
            targetFormats = setOf(
                org.jetbrains.compose.desktop.application.targets.NativeTargetTypes.Dmg,
                org.jetbrains.compose.desktop.application.targets.NativeTargetTypes.Msi,
                org.jetbrains.compose.desktop.application.targets.NativeTargetTypes.Deb
            )
            packageVersion = "1.0.0"
        }
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.window)
    implementation(libs.androidx.lifecycle.runtime)
}