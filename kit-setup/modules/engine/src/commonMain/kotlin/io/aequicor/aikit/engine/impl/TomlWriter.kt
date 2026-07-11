package io.aequicor.aikit.engine.impl

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Serialises a [JsonObject] tree into TOML text.
 *
 * Covers the subset needed for agent config files (e.g. Codex `.codex/config.toml`):
 * - scalars: strings (escaped basic strings), integers, floats, booleans;
 * - arrays of primitives (and nested arrays) as inline `[...]`;
 * - arrays of flat objects as inline tables `[{...}, ...]`;
 * - nested objects as dotted tables `[a]`, `[a.b]` — e.g. `[features]`, `[mcp_servers.github]`;
 * - flat objects nested deeper than one table level as inline tables (e.g. `env = { KEY = "v" }`).
 *
 * `null` values are skipped (TOML has no null). Key order follows [JsonObject] insertion order,
 * so output is deterministic for a given input tree.
 */
internal object TomlWriter {

    /** Table-nesting depth from which flat objects are emitted inline instead of as `[a.b.c]`. */
    private const val INLINE_TABLE_DEPTH = 3

    /** Render [root] as a complete TOML document (with a trailing newline). */
    fun write(root: JsonObject): String {
        val out = StringBuilder()
        writeTable(prefix = emptyList(), obj = root, out = out)
        return out.toString().trim('\n') + "\n"
    }

    private fun writeTable(prefix: List<String>, obj: JsonObject, out: StringBuilder) {
        val scalars = linkedMapOf<String, JsonElement>()
        val tables = linkedMapOf<String, JsonObject>()

        for ((key, value) in obj) {
            when {
                value is JsonNull -> Unit
                value is JsonObject && !isInline(value, tableDepth = prefix.size + 1) -> tables[key] = value
                else -> scalars[key] = value
            }
        }

        // A table header is emitted only when the table has own key-value pairs (or is empty);
        // pure containers of sub-tables (e.g. `mcp_servers`) need no header of their own —
        // `[mcp_servers.github]` implicitly defines them.
        val needsHeader = prefix.isNotEmpty() && (scalars.isNotEmpty() || tables.isEmpty())
        if (needsHeader) {
            out.append('[').append(prefix.joinToString(".") { formatKey(it) }).append("]\n")
        }
        for ((key, value) in scalars) {
            out.append(formatKey(key)).append(" = ").append(formatValue(value)).append('\n')
        }
        if (needsHeader || (prefix.isEmpty() && scalars.isNotEmpty())) out.append('\n')

        for ((key, value) in tables) {
            writeTable(prefix + key, value, out)
        }
    }

    /** Flat objects nested inside a named table render inline: `env = { KEY = "v" }`. */
    private fun isInline(obj: JsonObject, tableDepth: Int): Boolean =
        tableDepth >= INLINE_TABLE_DEPTH && obj.values.all { it is JsonPrimitive || it is JsonNull }

    private fun formatValue(value: JsonElement): String = when (value) {
        is JsonNull -> error("TOML has no null values")
        is JsonPrimitive -> formatPrimitive(value)
        is JsonArray -> value.joinToString(prefix = "[", postfix = "]") { formatValue(it) }
        is JsonObject -> formatInlineTable(value)
    }

    private fun formatInlineTable(obj: JsonObject): String =
        obj.entries
            .filter { it.value !is JsonNull }
            .joinToString(prefix = "{ ", postfix = " }") { (k, v) -> "${formatKey(k)} = ${formatValue(v)}" }
            .let { if (it == "{  }") "{}" else it }

    private fun formatPrimitive(value: JsonPrimitive): String = when {
        !value.isString -> value.content // bool / int / float — already valid TOML literals
        else -> "\"${escape(value.content)}\""
    }

    private fun formatKey(key: String): String =
        if (key.isNotEmpty() && key.all(::isBareKeyChar)) key else "\"${escape(key)}\""

    private fun isBareKeyChar(ch: Char): Boolean = ch.isLetterOrDigit() || ch == '_' || ch == '-'

    private fun escape(raw: String): String = buildString {
        for (ch in raw) {
            when {
                ch == '\\' -> append("\\\\")
                ch == '"' -> append("\\\"")
                ch == '\n' -> append("\\n")
                ch == '\r' -> append("\\r")
                ch == '\t' -> append("\\t")
                ch.code < MIN_PRINTABLE -> append("\\u").append(ch.code.toString(HEX_RADIX).padStart(UNICODE_WIDTH, '0'))
                else -> append(ch)
            }
        }
    }

    private const val MIN_PRINTABLE = 0x20
    private const val HEX_RADIX = 16
    private const val UNICODE_WIDTH = 4
}
