plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.detekt")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.core)
        }
    }
}

