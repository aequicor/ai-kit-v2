package io.aequicor.aikit.core.domain.projectManifest


/**
 * A concrete value supplied for a bundle input in [BundleTarget.inputs].
 * The concrete variant must be compatible with the [InputType] declared in the bundle's [InputSpec].
 */
sealed interface InputValue {

    /** Value for [InputType.BOOLEAN] inputs. */
    data class Bool(val value: Boolean) : InputValue

    /** Value for [InputType.STRING] and [InputType.SELECT] inputs. */
    data class Str(val value: String) : InputValue

    /** Value for [InputType.INT] inputs. */
    data class Int(val value: kotlin.Int) : InputValue

    /** Value for [InputType.DOUBLE] inputs. */
    data class Dbl(val value: Double) : InputValue

    /** Value for [InputType.MULTISELECT] inputs. */
    data class MultiSelect(val values: List<String>) : InputValue
}
