package io.aequicor.aikit.engine.pipeline

/**
 * Expands `${env:VAR}`, `${file:path}`, and `${envFile:path/file.env:KEY}` references
 * embedded in string values. Unknown `${...}` patterns pass through unchanged.
 */
internal object StringExpander {

    private const val KIND_GROUP = 1
    private const val ARG_GROUP = 2
    private const val MIN_QUOTED_LEN = 2
    private const val QUOTE_OFFSET = 1

    private val REF_REGEX = Regex("""\$\{(env|file|envFile):([^}]+)\}""")

    fun expand(value: String, reader: ValueSourceReader): Result<String> = runCatching {
        if (!value.contains("\${")) return@runCatching value
        val sb = StringBuilder()
        var lastIndex = 0
        for (match in REF_REGEX.findAll(value)) {
            sb.append(value, lastIndex, match.range.first)
            sb.append(resolve(match.groupValues[KIND_GROUP], match.groupValues[ARG_GROUP], reader))
            lastIndex = match.range.last + 1
        }
        sb.append(value.substring(lastIndex))
        sb.toString()
    }

    private fun resolve(kind: String, arg: String, reader: ValueSourceReader): String = when (kind) {
        "env" -> requireNotNull(reader.readEnv(arg)) { "env var '$arg' is not set" }

        "file" -> {
            validateRelativePath(arg)
            reader.readFile(arg)
                .getOrElse { throw IllegalArgumentException("cannot read file '$arg': ${it.message}") }
                .trimEnd()
        }

        "envFile" -> {
            val sep = arg.indexOf(':')
            require(sep >= 0) {
                "invalid \${envFile:...} — expected format \${envFile:path/file.env:KEY}, got: $arg"
            }
            val filePath = arg.substring(0, sep)
            val key = arg.substring(sep + 1)
            validateRelativePath(filePath)
            val content = reader.readFile(filePath)
                .getOrElse { throw IllegalArgumentException("cannot read file '$filePath': ${it.message}") }
            requireNotNull(parseEnvFile(content)[key]) { "key '$key' not found in '$filePath'" }
        }

        else -> "\${$kind:$arg}"
    }

    private fun validateRelativePath(path: String) {
        require(!path.startsWith("/") && !path.startsWith("\\")) {
            "path must be relative, got absolute path: '$path'"
        }
        require(path.length < MIN_QUOTED_LEN || path[QUOTE_OFFSET] != ':') {
            "path must be relative, got absolute path: '$path'"
        }
        val segments = path.split('/', '\\')
        require(segments.none { it == ".." }) {
            "path traversal ('..') is not allowed: '$path'"
        }
    }

    internal fun parseEnvFile(content: String): Map<String, String> =
        content.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() && !it.startsWith('#') }
            .mapNotNull { line ->
                val eq = line.indexOf('=')
                if (eq < 0) return@mapNotNull null
                val key = line.substring(0, eq).trim()
                if (key.isEmpty()) return@mapNotNull null
                val rawValue = line.substring(eq + 1).trim()
                key to stripQuotes(rawValue)
            }
            .toMap()

    private fun stripQuotes(value: String): String {
        if (value.length < MIN_QUOTED_LEN) return value
        val first = value.first()
        val last = value.last()
        return if (isMatchingQuote(first, last)) {
            value.substring(QUOTE_OFFSET, value.length - QUOTE_OFFSET)
        } else {
            value
        }
    }

    private fun isMatchingQuote(first: Char, last: Char): Boolean =
        (first == '"' && last == '"') || (first == '\'' && last == '\'')
}
