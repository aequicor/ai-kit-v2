package io.aequicor.aikit.io.remote

/**
 * Parsed coordinates of a bundle hosted in a GitHub repository.
 *
 * Textual form (as it appears after the `remote:` scheme in a bundle source reference):
 *
 * ```
 * remote:<owner>/<repo>/<path/inside/repo>[@<branch>]
 * ```
 *
 * Examples:
 * - `remote:aequicor/ai-kit-v2/my-bundle` — bundle in `my-bundle/` on the default branch.
 * - `remote:acme/presets/bundles/kotlin@stable` — nested path, branch `stable`.
 *
 * The bare manifest sentinel `source: "remote"` resolves to the default registry — this
 * repository, bundle folder named after the bundle (see [defaultFor]).
 *
 * All components are validated against strict character allow-lists so a reference can never
 * smuggle shell metacharacters into the git commands used to download it.
 *
 * @property owner GitHub account or organisation.
 * @property repo repository name.
 * @property path bundle directory inside the repository, POSIX `/` separators, no leading slash.
 * @property branch branch to track.
 */
data class RemoteBundleRef(
    val owner: String,
    val repo: String,
    val path: String,
    val branch: String,
) {

    /** Clone URL of the repository. */
    val repoUrl: String get() = "https://github.com/$owner/$repo.git"

    /** Canonical textual form, `remote:<owner>/<repo>/<path>@<branch>`. */
    fun toRefString(): String = "$SCHEME$owner/$repo/$path@$branch"

    companion object {

        /** Scheme prefix of remote bundle references. */
        const val SCHEME = "remote:"

        /** Manifest `source` sentinel that resolves to the default registry. */
        const val SOURCE_SENTINEL = "remote"

        private const val DEFAULT_OWNER = "aequicor"
        private const val DEFAULT_REPO = "ai-kit-v2"
        private const val DEFAULT_BRANCH = "main"
        private const val MIN_SEGMENTS = 3
        private const val PATH_START_INDEX = 2

        private val NAME_PATTERN = Regex("""[A-Za-z0-9._-]+""")
        private val BRANCH_PATTERN = Regex("""[A-Za-z0-9._/-]+""")

        /** Whether [ref] uses the `remote:` scheme. */
        fun matches(ref: String): Boolean = ref.startsWith(SCHEME)

        /**
         * Default-registry coordinates for a bundle named [bundleName]: this repository,
         * branch `main`, bundle directory `<bundleName>/` at the repo root.
         */
        fun defaultFor(bundle: String): Result<RemoteBundleRef> {
            val name = bundle.substringBefore('@')
            val version = bundle.substringAfter('@', missingDelimiterValue = "")
            return if (version.isEmpty()) {
                Result.failure(IllegalArgumentException("official remote bundle must include a version: '$bundle'"))
            } else {
                parse("$SCHEME$DEFAULT_OWNER/$DEFAULT_REPO/bundles/$name/$version@$DEFAULT_BRANCH")
            }
        }

        /**
         * Parse and validate a `remote:` reference.
         *
         * @return parsed coordinates, or failure describing the first violated rule.
         */
        fun parse(ref: String): Result<RemoteBundleRef> = runCatching {
            require(matches(ref)) { "not a remote bundle reference: '$ref'" }
            val body = ref.removePrefix(SCHEME)
            val coordinates = body.substringBeforeLast('@', body)
            val branch = if ('@' in body) body.substringAfterLast('@') else DEFAULT_BRANCH

            val segments = coordinates.split('/')
            require(segments.size >= MIN_SEGMENTS) {
                "remote reference must be '$SCHEME<owner>/<repo>/<path>[@<branch>]': '$ref'"
            }
            val owner = segments[0]
            val repo = segments[1]
            val pathSegments = segments.drop(PATH_START_INDEX)
            val path = pathSegments.joinToString("/")

            require(NAME_PATTERN.matches(owner)) { "invalid owner '$owner' in '$ref'" }
            require(NAME_PATTERN.matches(repo)) { "invalid repo '$repo' in '$ref'" }
            pathSegments.forEach { segment ->
                require(NAME_PATTERN.matches(segment) && segment != ".." && segment != ".") {
                    "invalid path segment '$segment' in '$ref'"
                }
            }
            require(BRANCH_PATTERN.matches(branch) && ".." !in branch) {
                "invalid branch '$branch' in '$ref'"
            }

            RemoteBundleRef(owner = owner, repo = repo, path = path, branch = branch)
        }
    }
}
