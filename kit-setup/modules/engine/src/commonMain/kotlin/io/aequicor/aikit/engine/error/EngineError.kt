package io.aequicor.aikit.engine.error

/** Errors produced by the engine pipeline. */
sealed class EngineError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** Manifest file could not be read or parsed. */
    class ManifestLoadError(message: String, cause: Throwable? = null) : EngineError(message, cause)

    /** A bundle referenced in the manifest could not be loaded. */
    class BundleLoadError(val bundleRef: String, message: String, cause: Throwable? = null) :
        EngineError(message, cause)

    /** An input value required by a bundle is missing or has the wrong type. */
    class InputValidationError(val inputId: String, message: String) : EngineError(message)

    /** A template file could not be rendered. */
    class RenderError(val templatePath: String, message: String, cause: Throwable? = null) :
        EngineError(message, cause)

    /** A rendered file could not be written to disk. */
    class WriteError(val outputPath: String, message: String, cause: Throwable? = null) :
        EngineError(message, cause)
}
