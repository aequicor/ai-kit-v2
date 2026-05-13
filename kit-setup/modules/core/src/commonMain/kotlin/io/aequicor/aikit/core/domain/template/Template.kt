package io.aequicor.aikit.core.domain.template

/**
 * A single template file shipped inside a bundle, held as raw source bytes.
 *
 * The domain layer is intentionally opaque: substitution (`${bundle.input.<id>}`) and conditional
 * blocks (`<!-- when: … --> … <!-- end -->`) are not parsed here. The linker stage consumes
 * [bytes] together with resolved input values and produces the final on-disk file.
 *
 * The same model covers both processable files (`.md` templates with AI-Kit extensions) and
 * verbatim assets (`.js` plugins / hooks for OpenCode, JSON snippets, binary files). The linker
 * decides how to treat each one based on [path].
 *
 * @property path Bundle-relative POSIX path to the source file (e.g. `claude/commands/review.md`).
 *   The linker writes the rendered result to the same relative path inside the target installation.
 * @property bytes Raw source content exactly as stored in the bundle.
 */
data class Template(
    val path: String,
    val bytes: ByteArray,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Template) return false
        return path == other.path && bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = 31 * path.hashCode() + bytes.contentHashCode()
}
