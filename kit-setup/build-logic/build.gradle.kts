plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.kotlin.multiplatform.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })
    implementation(libs.plugins.kotlin.serialization.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })
    implementation(libs.plugins.detekt.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })
}
