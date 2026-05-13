plugins {
    id("ai-kit.kotlin-module")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:core"))
            implementation(project(":modules:parsing"))
            implementation(libs.kotlinx.io.core)
        }
    }
}
