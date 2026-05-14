package io.aequicor.aikit.format.template

import kotlinx.serialization.Serializable

/**
 * A raw template file as it exists inside a bundle, before rendering or condition evaluation.
 *
 * Produced by the format layer when reading bundle contents. The engine evaluates [condition]
 * against resolved inputs to decide whether to include the file, renders substitutions and
 * conditional blocks, and writes the result as a final
 * [io.aequicor.aikit.core.domain.template.Template].
 *
 * @property path Bundle-relative POSIX path (e.g. `claude-code/commands/review.md`).
 * @property bytes Raw source content exactly as stored in the bundle.
 * @property condition Optional AKEL expression. When non-null, the engine skips the file if the
 *   expression evaluates to falsy against resolved inputs.
 */
@Serializable
data class TemplateSource(
    val path: String,
    val bytes: ByteArray,
    val condition: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemplateSource) return false
        return path == other.path && bytes.contentEquals(other.bytes) && condition == other.condition
    }

    override fun hashCode(): Int = 31 * (31 * path.hashCode() + bytes.contentHashCode()) + condition.hashCode()
}
