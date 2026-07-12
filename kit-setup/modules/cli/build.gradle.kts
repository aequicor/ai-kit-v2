plugins {
    id("ai-kit.kotlin-module")
    id("ai-kit.detekt")
}

val windowsManifestResource = layout.buildDirectory.file("generated/windows-resources/kit-setup.res")
val compileWindowsManifest by tasks.registering(Exec::class) {
    val manifestFile = layout.projectDirectory.file("src/mingwMain/resources/kit-setup.manifest")
    val resourceFile = layout.projectDirectory.file("src/mingwMain/resources/kit-setup.rc")

    inputs.files(manifestFile, resourceFile)
    outputs.file(windowsManifestResource)

    doFirst {
        val konanHome = System.getenv("KONAN_DATA_DIR")
            ?.let(::file)
            ?: file("${System.getProperty("user.home")}/.konan")
        val windres = konanHome.resolve("dependencies")
            .walkTopDown()
            .firstOrNull { it.isFile && it.name.equals("windres.exe", ignoreCase = true) }
            ?: error("windres.exe was not found under ${konanHome.resolve("dependencies")}")
        val clang = konanHome.resolve("dependencies")
            .walkTopDown()
            .firstOrNull { it.isFile && it.name.equals("clang.exe", ignoreCase = true) }
            ?: error("clang.exe was not found under ${konanHome.resolve("dependencies")}")
        val output = windowsManifestResource.get().asFile

        output.parentFile.mkdirs()
        commandLine(
            windres,
            "--preprocessor=\"${clang.absolutePath}\" -E -xc -DRC_INVOKED",
            "-I",
            resourceFile.asFile.parentFile,
            resourceFile.asFile,
            "-O",
            "coff",
            "-o",
            output,
        )
    }
}

kotlin {
    listOf(linuxX64(), macosArm64()).forEach { target ->
        target.binaries {
            executable {
                entryPoint = "io.aequicor.aikit.cli.main"
            }
        }
    }

    mingwX64 {
        binaries {
            executable {
                entryPoint = "io.aequicor.aikit.cli.main"
                linkerOpts(windowsManifestResource.get().asFile.absolutePath)
                linkTaskProvider.configure {
                    dependsOn(compileWindowsManifest)
                }
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
