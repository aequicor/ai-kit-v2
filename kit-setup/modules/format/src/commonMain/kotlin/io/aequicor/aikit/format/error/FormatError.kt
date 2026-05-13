package io.aequicor.aikit.format.error

/**
 * Errors produced by the `format` module during parsing or mapping.
 *
 * All public APIs in `format` return `Result<T>`; these are the [Throwable] subclasses placed
 * inside failed results. They are not thrown.
 */
sealed class FormatError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Raw text could not be decoded as JSON, or the JSON structure was invalid. */
    class BadJson(message: String, cause: Throwable? = null) : FormatError(message, cause)

    /** `schemaVersion` value is not supported by this version of the CLI. */
    class UnsupportedSchemaVersion(version: Int, supported: IntRange) :
        FormatError("unsupported schemaVersion $version, supported: $supported")

    /** A required field is missing or holds a null that is not allowed. */
    class MissingField(field: String, context: String) :
        FormatError("missing required field '$field' in $context")

    /** An enum string did not match any known value. */
    class UnknownEnum(value: String, field: String, known: Set<String>) :
        FormatError("unknown value '$value' for field '$field', known: ${known.sorted()}")

    /** Template body has unmatched `<!-- when -->` / `<!-- end -->` delimiters. */
    class MalformedTemplate(path: String, detail: String) :
        FormatError("malformed template '$path': $detail")

    /** A `\${bundle.input.<id>}` substitution references an input id not in the bundle manifest. */
    class UnknownInputReference(inputId: String, templatePath: String) :
        FormatError("template '$templatePath' references unknown input '\${bundle.input.$inputId}'")
}
