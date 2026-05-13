plugins {
    id("ai-kit.kotlin-module")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":modules:core"))
        }
    }
}
