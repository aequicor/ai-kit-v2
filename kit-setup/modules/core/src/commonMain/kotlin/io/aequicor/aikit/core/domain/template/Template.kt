package io.aequicor.aikit.core.domain.template

/**
 * A resolved template file ready to be written to disk.
 *
 * By the time a [Template] exists in the domain, all conditions have been evaluated and the
 * file has been selected for inclusion. The [bytes] hold the final rendered content — all
 * `${bundle.input.<id>}` substitutions and `<!-- when: … -->…<!-- end -->` conditional blocks
 * have been processed by the engine before this model is produced.
 *
 * @property path Installation-root-relative POSIX path (e.g. `commands/review.md`).
 * @property bytes Final rendered file content.
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
