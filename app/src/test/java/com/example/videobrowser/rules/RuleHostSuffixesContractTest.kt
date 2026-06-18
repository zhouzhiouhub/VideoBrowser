package com.example.videobrowser.rules

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleHostSuffixesContractTest {
    @Test
    fun `rule indexes share host suffix expansion`() {
        assertEquals(
            listOf("m.example.com", "example.com", "com"),
            RuleHostSuffixes.forHost("M.Example.COM")
        )

        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/rules/ElementRuleIndex.kt"),
            projectFile("src/main/java/com/example/videobrowser/rules/RequestRuleIndex.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("RuleHostSuffixes.forHost("))
            assertFalse(source.contains("private fun hostSuffixes"))
        }
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
