package io.aequicor.aikit.akel

/**
 * Runtime type tag for an [AkelValue].
 *
 * Used by the evaluator to report type-mismatch errors with a readable type name.
 */
enum class AkelType {
    /** Boolean. */
    BOOL,

    /** 32-bit signed integer. */
    INT,

    /** 64-bit double. */
    DOUBLE,

    /** UTF-16 string. */
    STRING,

    /** Homogeneous list. Element type is determined at evaluation time. */
    LIST,
}
