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
    ":modules:parsing",
    ":modules:agent-layout",
    ":modules:validation",
    ":modules:bundle-resolver",
    ":modules:generation",
    ":modules:cli",
)
