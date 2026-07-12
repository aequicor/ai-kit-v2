package io.aequicor.aikit.io.process

/** Converts platform filesystem separators to the slash form accepted by git on every target. */
fun processPath(path: String): String = path.replace('\\', '/')
