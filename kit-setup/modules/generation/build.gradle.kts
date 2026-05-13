plugins {
    id("ai-kit.kotlin-module")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:core"))
            implementation(project(":modules:agent-layout"))
            implementation(libs.kotlinx.io.core)
        }
    }
}
