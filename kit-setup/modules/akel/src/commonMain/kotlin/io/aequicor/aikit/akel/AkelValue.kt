package io.aequicor.aikit.akel

/**
 * A typed value in the AKEL runtime.
 *
 * Values are produced by literal expressions and by resolving `${bundle.input.<id>}` references
 * through an [AkelContext]. AKEL is strictly typed: there are no implicit conversions between
 * variants (see [AkelType]).
 */
sealed interface AkelValue {

    /** Runtime type tag — used by the evaluator for type-mismatch error messages. */
    val type: AkelType

    /** Boolean value. */
    data class Bool(val value: Boolean) : AkelValue {
        override val type: AkelType get() = AkelType.BOOL
    }

    /** Integer value. */
    data class Int(val value: kotlin.Int) : AkelValue {
        override val type: AkelType get() = AkelType.INT
    }

    /** Double-precision floating-point value. */
    data class Dbl(val value: Double) : AkelValue {
        override val type: AkelType get() = AkelType.DOUBLE
    }

    /** String value. */
    data class Str(val value: String) : AkelValue {
        override val type: AkelType get() = AkelType.STRING
    }

    /**
     * List value.
     *
     * Element types are not enforced by the constructor; the evaluator validates homogeneity
     * when the list participates in an `in` or equality operation.
     */
    data class Lst(val elements: List<AkelValue>) : AkelValue {
        override val type: AkelType get() = AkelType.LIST
    }
}
