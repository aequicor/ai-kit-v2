package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

data class KitResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) {
    val combined: String get() = stdout + stderr
}

object KitRunner {

    private val binaryPath: Path by lazy {
        val raw = System.getProperty("kit.binary")
            ?: error("System property 'kit.binary' is not set. Pass -Dkit.binary=... or -Pkit.binary=...")
        val path = Paths.get(raw).toAbsolutePath().normalize()
        check(path.exists()) { "Kit binary not found at: $path" }
        check(Files.isExecutable(path)) { "Kit binary is not executable: $path" }
        path
    }

    fun run(
        vararg args: String,
        cwd: Path = Files.createTempDirectory("aikit-e2e-cwd-"),
        timeoutSeconds: Long = 60,
    ): KitResult {
        val cmd = buildList {
            add(binaryPath.absolutePathString())
            addAll(args)
        }
        val process = ProcessBuilder(cmd)
            .directory(cwd.toFile())
            .redirectErrorStream(false)
            .start()

        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val outThread = Thread {
            process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) }
        }.apply { isDaemon = true; start() }
        val errThread = Thread {
            process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) }
        }.apply { isDaemon = true; start() }

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
            error("kit-setup ${args.joinToString(" ")} timed out after ${timeoutSeconds}s")
        }
        outThread.join(5_000)
        errThread.join(5_000)

        return KitResult(
            exitCode = process.exitValue(),
            stdout = stdout.toString(),
            stderr = stderr.toString(),
        )
    }
}
