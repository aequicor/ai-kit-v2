plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.kotlin-serialization")
    id("ai-kit.detekt")
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
        }
    }
}

// Native test binaries run from projectDir so that
// src/commonTest/resources/ paths resolve correctly.
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>().configureEach {
    workingDir = projectDir.absolutePath
}
