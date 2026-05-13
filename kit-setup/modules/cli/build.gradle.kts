plugins {
    id("ai-kit.kotlin-module")
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().configureEach {
        binaries {
            executable {
                entryPoint = "io.aequicor.aikit.cli.main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:core"))
            implementation(project(":modules:parsing"))
            implementation(project(":modules:agent-layout"))
            implementation(project(":modules:validation"))
            implementation(project(":modules:bundle-resolver"))
            implementation(project(":modules:generation"))
            implementation(libs.clikt)
        }
    }
}
