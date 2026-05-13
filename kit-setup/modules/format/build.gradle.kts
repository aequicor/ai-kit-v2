plugins {
    id("ai-kit.kotlin-serialization")
    id("ai-kit.detekt")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:core"))
            implementation(project(":modules:io"))
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.io.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest>().configureEach {
    workingDir = projectDir.absolutePath
}
