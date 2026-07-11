package io.aequicor.aikit.io.process

/**
 * Outcome of running an external command.
 *
 * @property exitCode process exit code; `0` means success.
 * @property output combined stdout + stderr of the process, decoded as UTF-8.
 */
data class ProcessResult(
    val exitCode: Int,
    val output: String,
)

/**
 * Minimal abstraction over launching an external command.
 *
 * The command is provided as an argv-style token list. Implementations MUST reject tokens
 * containing shell metacharacters — the production implementation delegates to the platform
 * shell (`popen`), so validation is the only barrier against command injection. Callers must
 * only pass tokens validated against [isSafeProcessToken].
 */
fun interface ProcessRunner {

    /**
     * Run [command] (argv tokens: executable first) and wait for completion.
     *
     * @param command argv tokens; every token must satisfy [isSafeProcessToken].
     * @return [ProcessResult] on spawn success (non-zero exit codes are NOT failures),
     *   or failure when the process could not be started or a token is unsafe.
     */
    fun run(command: List<String>): Result<ProcessResult>
}

/**
 * Whether [token] is safe to pass through a shell without quoting concerns.
 *
 * Allows only characters that appear in git URLs, refs, file paths and CLI flags:
 * ASCII letters, digits and `@ . _ : / + = ~ -`. Everything else — whitespace, quotes,
 * `; | & $ < > ( ) \` and any control character — is rejected.
 */
fun isSafeProcessToken(token: String): Boolean =
    token.isNotEmpty() && token.all { ch ->
        ch.isLetterOrDigit() && ch.code < NON_ASCII_BOUNDARY || ch in SAFE_PUNCTUATION
    }

private const val NON_ASCII_BOUNDARY = 128
private const val SAFE_PUNCTUATION = "@._:/+=~-"

/** Production [ProcessRunner] backed by the platform shell. */
class DefaultProcessRunner : ProcessRunner {

    override fun run(command: List<String>): Result<ProcessResult> = runCatching {
        require(command.isNotEmpty()) { "command must not be empty" }
        command.forEach { token ->
            require(isSafeProcessToken(token)) { "unsafe process token: '$token'" }
        }
        // Tokens are validated above; joining with spaces is safe because no token can
        // contain whitespace or shell metacharacters. stderr is merged into stdout so
        // error messages from git/curl surface in the result.
        execCommand(command.joinToString(" ") + " 2>&1")
    }
}

/**
 * Launch [commandLine] via the platform shell, wait for completion, capture combined output.
 *
 * @throws IllegalStateException when the process cannot be spawned.
 */
internal expect fun execCommand(commandLine: String): ProcessResult
