@file:Suppress("MagicNumber")

package io.aequicor.aikit.core.domain.bundle

/** Minimal SemVer 2.0 value and AND-only comparator range used by bundle compatibility. */
data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: List<String> = emptyList(),
) : Comparable<SemVer> {
    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    override fun compareTo(other: SemVer): Int {
        compareValues(major, other.major).takeIf { it != 0 }?.let { return it }
        compareValues(minor, other.minor).takeIf { it != 0 }?.let { return it }
        compareValues(patch, other.patch).takeIf { it != 0 }?.let { return it }
        if (preRelease.isEmpty() || other.preRelease.isEmpty()) {
            return when {
                preRelease.isEmpty() && other.preRelease.isEmpty() -> 0
                preRelease.isEmpty() -> 1
                else -> -1
            }
        }
        for (index in 0 until minOf(preRelease.size, other.preRelease.size)) {
            val left = preRelease[index]
            val right = other.preRelease[index]
            val leftNumber = left.toIntOrNull()
            val rightNumber = right.toIntOrNull()
            val result = when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right)
            }
            if (result != 0) return result
        }
        return preRelease.size.compareTo(other.preRelease.size)
    }

    companion object {
        private val PATTERN = Regex(
            """^(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)""" +
                """(?:-([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?(?:\+[0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*)?$"""
        )

        fun parse(value: String): Result<SemVer> = runCatching {
            val match = requireNotNull(PATTERN.matchEntire(value.trim())) {
                "invalid SemVer '$value'"
            }
            SemVer(
                match.groupValues[1].toInt(),
                match.groupValues[2].toInt(),
                match.groupValues[3].toInt(),
                match.groupValues[4].takeIf(String::isNotEmpty)?.split('.').orEmpty(),
            )
        }
    }
}

class SemVerRange private constructor(private val comparators: List<Comparator>) {
    fun contains(version: SemVer): Boolean = comparators.all { it.matches(version) }

    private data class Comparator(val operator: String, val version: SemVer) {
        fun matches(candidate: SemVer): Boolean = when (operator) {
            ">" -> candidate > version
            ">=" -> candidate >= version
            "<" -> candidate < version
            "<=" -> candidate <= version
            "=" -> candidate == version
            else -> false
        }
    }

    companion object {
        private val COMPARATOR = Regex("""(>=|<=|>|<|=)([^\s]+)""")

        fun parse(value: String): Result<SemVerRange> = runCatching {
            val parts = value.trim().split(Regex("""\s+""")).filter(String::isNotEmpty)
            require(parts.isNotEmpty()) { "empty kitSetup range" }
            SemVerRange(parts.map { part ->
                val match = requireNotNull(COMPARATOR.matchEntire(part)) {
                    "invalid comparator '$part' in kitSetup range '$value'"
                }
                Comparator(match.groupValues[1], SemVer.parse(match.groupValues[2]).getOrThrow())
            })
        }
    }
}

object BundleCompatibility {
    @Suppress("ReturnCount")
    fun incompatibility(manifest: BundleManifest, currentVersion: String): String? {
        if (manifest.schemaVersion < 2) return "bundle schemaVersion ${manifest.schemaVersion} is obsolete; schemaVersion 2 is required"
        val required = manifest.kitSetup
            ?: return "bundle does not declare required kitSetup compatibility range"
        val current = SemVer.parse(currentVersion).getOrElse { return it.message ?: "invalid kit-setup version" }
        val range = SemVerRange.parse(required).getOrElse { return it.message ?: "invalid kitSetup range" }
        return if (range.contains(current)) null
        else "bundle requires kit-setup '$required', current version is '$currentVersion'"
    }
}
