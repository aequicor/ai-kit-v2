package io.aequicor.aikit.core.domain.bundle


/**
 * Specification of a single user-facing input parameter declared in [BundleManifest.inputs].
 *
 * The concrete variant determines which fields are applicable; the JSON `type` field is used as
 * the polymorphic discriminator (values match [SerialName] of each variant).
 *
 * All variants share [id], [title], and [description].
 */
sealed interface InputSpec {

    /** Unique identifier within the manifest. */
    val id: String

    /** Short prompt shown to the user during interactive installation. */
    val title: String

    /** Optional longer explanation shown below [title]; used by AI agents for automatic value selection. */
    val description: String?

    /**
     * Toggle: `true` / `false`.
     * Activates or skips the component folder named after [id].
     *
     * @property default Default boolean value; `null` means no default.
     */
    data class BoolInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val default: Boolean? = null,
    ) : InputSpec

    /**
     * Exactly one value chosen from [options].
     * Activates the component sub-folder matching the selected value.
     *
     * @property options Exhaustive list of accepted values. Required.
     * @property default Default option value; must be one of [options] when non-null.
     *   May contain `${cwd.basename}` context placeholders resolved by the CLI.
     */
    data class SelectInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val options: List<String>,
        val default: String? = null,
    ) : InputSpec

    /**
     * One or more values chosen from [options].
     * Activates all component sub-folders matching the selected values.
     *
     * @property options Exhaustive list of accepted values. Required.
     * @property default Default subset of [options]; `null` means no default.
     */
    data class MultiSelectInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val options: List<String>,
        val default: List<String>? = null,
    ) : InputSpec

    /**
     * Free-form string.
     * Not linked to any folder; substituted into templates via `${bundle.input.<id>}`.
     *
     * @property default Default string value; may contain `${cwd.basename}` placeholders.
     * @property required If `true`, an empty or blank value is rejected.
     */
    data class StringInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val default: String? = null,
        val required: Boolean? = null,
    ) : InputSpec

    /**
     * Whole number.
     * Substituted into templates; optionally validated against [min]/[max].
     *
     * @property default Default integer value.
     * @property required If `true`, the field must be provided.
     * @property min Inclusive lower bound; `null` means unbounded.
     * @property max Inclusive upper bound; `null` means unbounded.
     */
    data class IntInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val default: Int? = null,
        val required: Boolean? = null,
        val min: Int? = null,
        val max: Int? = null,
    ) : InputSpec

    /**
     * Floating-point number.
     * Substituted into templates; optionally validated against [min]/[max].
     *
     * @property default Default double value.
     * @property required If `true`, the field must be provided.
     * @property min Inclusive lower bound; `null` means unbounded.
     * @property max Inclusive upper bound; `null` means unbounded.
     */
    data class DoubleInput(
        override val id: String,
        override val title: String,
        override val description: String? = null,
        val default: Double? = null,
        val required: Boolean? = null,
        val min: Double? = null,
        val max: Double? = null,
    ) : InputSpec
}
