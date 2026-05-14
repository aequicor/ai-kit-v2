package io.aequicor.aikit.e2e

import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail

fun assertSuccess(result: KitResult, message: String = "kit-setup invocation") {
    assertEquals(
        0, result.exitCode,
        "$message expected exit 0 but got ${result.exitCode}\n--- stdout ---\n${result.stdout}\n--- stderr ---\n${result.stderr}",
    )
}

fun assertFailure(result: KitResult, message: String = "kit-setup invocation") {
    assertNotEquals(
        0, result.exitCode,
        "$message expected non-zero exit but got 0\n--- stdout ---\n${result.stdout}\n--- stderr ---\n${result.stderr}",
    )
}

fun assertFileExists(file: Path) {
    assertTrue(Files.isRegularFile(file), "Expected file to exist: $file")
}

fun assertFileAbsent(file: Path) {
    if (Files.exists(file)) fail<Unit>("Expected file to NOT exist: $file")
}

fun assertFileContains(file: Path, needle: String) {
    assertFileExists(file)
    val content = Files.readString(file)
    assertTrue(content.contains(needle)) {
        "Expected $file to contain \"$needle\".\nActual content:\n$content"
    }
}

fun assertStdoutContains(result: KitResult, needle: String) {
    assertTrue(result.combined.contains(needle)) {
        "Expected output to contain \"$needle\".\n--- stdout ---\n${result.stdout}\n--- stderr ---\n${result.stderr}"
    }
}
