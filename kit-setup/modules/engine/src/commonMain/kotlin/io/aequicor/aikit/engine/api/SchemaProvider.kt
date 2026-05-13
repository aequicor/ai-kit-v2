package io.aequicor.aikit.engine.api

/** Returns the JSON Schema for `.aikit/manifest.json`, used by the `aikit schema` command. */
fun interface SchemaProvider {
    fun schema(): String
}
