plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.detekt")
}

kotlin {
    listOf(linuxX64(), macosArm64(), mingwX64()).forEach { target ->
        target.binaries {
            executable {
                entryPoint = "io.aequicor.aikit.cli.main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:engine"))
            implementation(libs.clikt)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
