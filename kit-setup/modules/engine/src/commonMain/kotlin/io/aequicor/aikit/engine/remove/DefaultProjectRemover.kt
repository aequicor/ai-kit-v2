package io.aequicor.aikit.engine.remove

import io.aequicor.aikit.engine.lock.HashProvider
import io.aequicor.aikit.engine.lock.LockFileEntry
import io.aequicor.aikit.engine.lock.LockStore
import io.aequicor.aikit.engine.lock.LockTarget
import io.aequicor.aikit.engine.write.FileWriter
import kotlinx.io.files.Path

private const val MANIFEST_RELATIVE_PATH = ".aikit/manifest.json"
private const val AIKIT_DIR = ".aikit"

/** Filesystem-backed remover that uses [LockStore] as the registry of tracked files. */
internal class DefaultProjectRemover(
    private val fileWriter: FileWriter,
    private val lockStore: LockStore,
    private val hashProvider: HashProvider,
) : ProjectRemover {

    @Suppress("CyclomaticComplexMethod", "LongMethod")
    override fun remove(projectRoot: String, options: RemoveOptions): Result<RemoveReport> =
        runCatching {
            val lock = lockStore.read(projectRoot).getOrThrow()
                ?: throw LockNotFoundError(lockStore.pathOf(projectRoot))

            val deleted = mutableListOf<String>()
            val skipped = mutableListOf<String>()
            val missing = mutableListOf<String>()

            val tracked: List<LockFileEntry> = lock.applications
                .flatMap { it.targets.values.flatMap(LockTarget::files) }

            for (entry in tracked) {
                val abs = Path(projectRoot, entry.path).toString()
                if (!fileWriter.exists(abs)) {
                    missing += entry.path
                    continue
                }
                val current = fileWriter.read(abs).map(hashProvider::hash).getOrNull()
                if (current == entry.hash || options.force) {
                    if (!options.dryRun) {
                        fileWriter.delete(abs).getOrThrow()
                        pruneEmptyParents(projectRoot, entry.path)
                    }
                    deleted += entry.path
                } else {
                    skipped += entry.path
                }
            }

            var manifestRemoved = false
            if (!options.keepManifest && !options.dryRun) {
                listOf(MANIFEST_RELATIVE_PATH, ".aikit/manifest.lock.json").forEach { rel ->
                    val abs = Path(projectRoot, rel).toString()
                    if (fileWriter.exists(abs)) {
                        fileWriter.delete(abs).getOrThrow()
                        manifestRemoved = true
                    }
                }
                fileWriter.deleteDirectoryIfEmpty(Path(projectRoot, AIKIT_DIR).toString())
            } else if (!options.keepManifest && options.dryRun) {
                val manifestAbs = Path(projectRoot, MANIFEST_RELATIVE_PATH).toString()
                val lockAbs = Path(projectRoot, ".aikit/manifest.lock.json").toString()
                manifestRemoved = fileWriter.exists(manifestAbs) || fileWriter.exists(lockAbs)
            }

            RemoveReport(
                deleted = deleted,
                skippedDrift = skipped,
                missing = missing,
                manifestRemoved = manifestRemoved,
                dryRun = options.dryRun,
            )
        }

    private fun pruneEmptyParents(projectRoot: String, relPath: String) {
        var current = relPath.substringBeforeLast('/', missingDelimiterValue = "")
        while (current.isNotEmpty()) {
            fileWriter.deleteDirectoryIfEmpty(Path(projectRoot, current).toString())
            current = current.substringBeforeLast('/', missingDelimiterValue = "")
        }
    }
}
