package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ByteSizeFormatterContractTest {
    @Test
    fun `local files and browser data share byte size formatting`() {
        val localDocumentFormatter = projectFile(
            "src/main/java/com/example/videobrowser/localfiles/LocalDocumentFormatter.kt"
        ).readText()
        val browserDataDisplayFormatter = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/BrowserDataDisplayFormatter.kt"
        ).readText()

        listOf(localDocumentFormatter, browserDataDisplayFormatter).forEach { source ->
            assertTrue(source.contains("ByteSizeFormatter.format("))
            assertFalse(source.contains("arrayOf(\"B\", \"KB\""))
            assertFalse(source.contains("while (value >= 1024"))
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
