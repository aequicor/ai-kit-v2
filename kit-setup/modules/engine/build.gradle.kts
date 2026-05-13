plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.detekt")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:core"))
            implementation(project(":modules:io"))
            implementation(project(":modules:format"))
            implementation(project(":modules:akel"))
        }
    }
}
