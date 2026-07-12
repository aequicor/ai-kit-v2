plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.detekt")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.io.core)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>().configureEach {
    workingDir = projectDir.absolutePath
}
