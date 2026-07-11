@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.aequicor.aikit.io.process

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.fgets
import platform.posix.pclose
import platform.posix.popen

private const val READ_BUFFER_SIZE = 4096
private const val EXIT_STATUS_SHIFT = 8
private const val EXIT_STATUS_MASK = 0xFF

// Keep in sync with ProcessRunner.linux.kt — POSIX popen/pclose semantics are identical,
// but the two targets cannot share a source set without customising the hierarchy template.
internal actual fun execCommand(commandLine: String): ProcessResult {
    val pipe = popen(commandLine, "r") ?: error("cannot spawn process: $commandLine")
    val output = StringBuilder()
    memScoped {
        val buffer = allocArray<ByteVar>(READ_BUFFER_SIZE)
        while (fgets(buffer, READ_BUFFER_SIZE, pipe) != null) {
            output.append(buffer.toKString())
        }
    }
    val rawStatus = pclose(pipe)
    check(rawStatus != -1) { "cannot obtain process exit status: $commandLine" }
    // waitpid-style status: exit code lives in bits 8..15.
    val exitCode = (rawStatus shr EXIT_STATUS_SHIFT) and EXIT_STATUS_MASK
    return ProcessResult(exitCode = exitCode, output = output.toString())
}
