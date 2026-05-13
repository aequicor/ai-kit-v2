plugins {
    id("ai-kit.kotlin-serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:core"))
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
