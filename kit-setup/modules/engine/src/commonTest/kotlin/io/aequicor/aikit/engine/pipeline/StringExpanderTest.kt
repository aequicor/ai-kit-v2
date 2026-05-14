package io.aequicor.aikit.engine.pipeline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StringExpanderTest {

    private fun reader(
        env: Map<String, String> = emptyMap(),
        files: Map<String, String> = emptyMap(),
    ): ValueSourceReader = object : ValueSourceReader {
        override fun readEnv(name: String): String? = env[name]
        override fun readFile(relativePath: String): Result<String> =
            files[relativePath]?.let { Result.success(it) }
                ?: Result.failure(Exception("file not found: '$relativePath'"))
    }

    // ─── env ───────────────────────────────────────────────────────────────

    @Test fun envSubstitution() {
        val result = StringExpander.expand("\${env:APP}", reader(env = mapOf("APP" to "billing"))).getOrThrow()
        assertEquals("billing", result)
    }

    @Test fun envPartialSubstitution() {
        val r = reader(env = mapOf("NS" to "acme", "APP" to "billing"))
        assertEquals("acme-billing-svc", StringExpander.expand("\${env:NS}-\${env:APP}-svc", r).getOrThrow())
    }

    @Test fun envMissingThrows() {
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${env:MISSING}", reader()).getOrThrow()
        }
        assertTrue(e.message?.contains("MISSING") == true)
    }

    // ─── file ──────────────────────────────────────────────────────────────

    @Test fun fileSubstitution() {
        val r = reader(files = mapOf("secrets/name.txt" to "my-project\n"))
        assertEquals("my-project", StringExpander.expand("\${file:secrets/name.txt}", r).getOrThrow())
    }

    @Test fun fileMissingThrows() {
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${file:missing.txt}", reader()).getOrThrow()
        }
        assertTrue(e.message?.contains("missing.txt") == true)
    }

    @Test fun fileAbsolutePathRejected() {
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${file:/etc/passwd}", reader()).getOrThrow()
        }
        assertTrue(e.message?.contains("absolute") == true)
    }

    @Test fun filePathTraversalRejected() {
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${file:../../secret}", reader()).getOrThrow()
        }
        assertTrue(e.message?.contains("..") == true)
    }

    @Test fun fileWindowsAbsolutePathRejected() {
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${file:C:/secret}", reader()).getOrThrow()
        }
        assertTrue(e.message?.contains("absolute") == true)
    }

    // ─── envFile ───────────────────────────────────────────────────────────

    @Test fun envFileSubstitution() {
        val dotenv = "KEY=hello\n"
        val r = reader(files = mapOf("config/local.env" to dotenv))
        assertEquals("hello", StringExpander.expand("\${envFile:config/local.env:KEY}", r).getOrThrow())
    }

    @Test fun envFileKeyNotFoundThrows() {
        val r = reader(files = mapOf(".env" to "OTHER=x\n"))
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${envFile:.env:MISSING}", r).getOrThrow()
        }
        assertTrue(e.message?.contains("MISSING") == true)
    }

    @Test fun envFileCommentsIgnored() {
        val dotenv = "# this is a comment\nKEY=value\n"
        val r = reader(files = mapOf(".env" to dotenv))
        assertEquals("value", StringExpander.expand("\${envFile:.env:KEY}", r).getOrThrow())
    }

    @Test fun envFileQuotedValuesStripped() {
        val dotenv = """KEY="quoted value"""" + "\nKEY2='single'"
        val r = reader(files = mapOf(".env" to dotenv))
        assertEquals("quoted value", StringExpander.expand("\${envFile:.env:KEY}", r).getOrThrow())
        assertEquals("single", StringExpander.expand("\${envFile:.env:KEY2}", r).getOrThrow())
    }

    @Test fun envFileMissingColonThrows() {
        val r = reader(files = mapOf(".env" to "K=v"))
        val e = assertFailsWith<IllegalArgumentException> {
            StringExpander.expand("\${envFile:.env}", r).getOrThrow()
        }
        assertTrue(e.message?.isNotEmpty() == true)
    }

    // ─── misc ──────────────────────────────────────────────────────────────

    @Test fun unknownKindPassesThrough() {
        val result = StringExpander.expand("\${foo:bar}", reader()).getOrThrow()
        assertEquals("\${foo:bar}", result)
    }

    @Test fun emptyStringNoOp() {
        assertEquals("", StringExpander.expand("", reader()).getOrThrow())
    }

    @Test fun noRefNoOp() {
        assertEquals("plain text", StringExpander.expand("plain text", reader()).getOrThrow())
    }

    @Test fun multipleRefsInOneString() {
        val r = reader(env = mapOf("A" to "hello", "B" to "world"))
        assertEquals("hello world", StringExpander.expand("\${env:A} \${env:B}", r).getOrThrow())
    }

    // ─── parseEnvFile ──────────────────────────────────────────────────────

    @Test fun parseEnvFileBasic() {
        val map = StringExpander.parseEnvFile("FOO=bar\nBAZ=qux")
        assertEquals("bar", map["FOO"])
        assertEquals("qux", map["BAZ"])
    }

    @Test fun parseEnvFileSkipsBlanksAndComments() {
        val map = StringExpander.parseEnvFile("\n# comment\nKEY=val\n\n")
        assertEquals(1, map.size)
        assertEquals("val", map["KEY"])
    }

    @Test fun parseEnvFileNoEqualsSkipped() {
        val map = StringExpander.parseEnvFile("NOEQUALS\nGOOD=ok")
        assertEquals(1, map.size)
        assertEquals("ok", map["GOOD"])
    }
}
