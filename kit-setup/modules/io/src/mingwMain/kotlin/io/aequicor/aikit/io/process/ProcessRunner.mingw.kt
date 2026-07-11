@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.aequicor.aikit.io.process

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix._pclose
import platform.posix._popen
import platform.posix.fgets

private const val READ_BUFFER_SIZE = 4096

internal actual fun execCommand(commandLine: String): ProcessResult {
    val pipe = _popen(commandLine, "r") ?: error("cannot spawn process: $commandLine")
    val output = StringBuilder()
    memScoped {
        val buffer = allocArray<ByteVar>(READ_BUFFER_SIZE)
        while (fgets(buffer, READ_BUFFER_SIZE, pipe) != null) {
            output.append(buffer.toKString())
        }
    }
    val exitCode = _pclose(pipe)
    check(exitCode != -1) { "cannot obtain process exit status: $commandLine" }
    // _pclose returns the cmd.exe exit code directly (no waitpid encoding on Windows).
    return ProcessResult(exitCode = exitCode, output = output.toString())
}
