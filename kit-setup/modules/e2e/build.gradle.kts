plugins {
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.kotlinx.serialization.json)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    jvmToolchain(21)
}

private fun defaultBinaryPath(): String {
    val binDir = rootDir.resolve("modules/cli/build/bin")
    val candidates = listOf(
        binDir.resolve("linuxX64/releaseExecutable/cli.kexe"),
        binDir.resolve("linuxX64/debugExecutable/cli.kexe"),
        binDir.resolve("macosArm64/releaseExecutable/cli.kexe"),
        binDir.resolve("macosArm64/debugExecutable/cli.kexe"),
        binDir.resolve("mingwX64/releaseExecutable/cli.exe"),
        binDir.resolve("mingwX64/debugExecutable/cli.exe"),
    )
    return (candidates.firstOrNull { it.exists() } ?: candidates.first()).absolutePath
}

tasks.test {
    useJUnitPlatform()
    val binary = providers.gradleProperty("kit.binary")
        .orElse(providers.systemProperty("kit.binary"))
        .orElse(provider { defaultBinaryPath() })
    systemProperty("kit.binary", binary.get())
    systemProperty("kit.bundlesDir", rootDir.parentFile.resolve("bundles").absolutePath)
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
        showExceptions = true
    }
}

// E2E tests require a pre-built native binary. Detach them from `check`/`build`
// so a regular `./gradlew build` does not require linking the CLI first.
// Run explicitly: `./gradlew :modules:e2e:test -Pkit.binary=<path-to-cli>`.
tasks.named("check") {
    setDependsOn(emptyList<Any>())
}
