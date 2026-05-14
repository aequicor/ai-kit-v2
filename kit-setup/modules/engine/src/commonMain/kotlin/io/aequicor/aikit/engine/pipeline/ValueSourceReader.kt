package io.aequicor.aikit.engine.pipeline

import kotlin.Result

/** Reads values from external sources (env vars, files) during input expansion. */
internal interface ValueSourceReader {
    fun readEnv(name: String): String?
    fun readFile(relativePath: String): Result<String>
}

/** Creates the default filesystem-backed [ValueSourceReader] for [projectRoot]. */
internal expect fun createDefaultValueSourceReader(projectRoot: String): ValueSourceReader
