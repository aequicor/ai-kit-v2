rootProject.name = "ai-kit-v2"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(
    ":modules:core",
    ":modules:cli",
    ":modules:akel",
    ":modules:io",
    ":modules:format",
    ":modules:engine",
)
