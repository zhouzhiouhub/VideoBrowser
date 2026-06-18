package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebUrlNormalizerContractTest {
    @Test
    fun `saved pages and tab sessions share restorable web url validation`() {
        val savedPageCodec = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageCodec.kt"
        ).readText()
        val tabSessionRepository = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserTabSessionRepository.kt"
        ).readText()

        listOf(savedPageCodec, tabSessionRepository).forEach { source ->
            assertTrue(source.contains("WebUrlNormalizer.normalizeHttpOrHttpsUrl"))
            assertFalse(source.contains("scheme != \"http\" && scheme != \"https\""))
            assertFalse(source.contains("uri.host.isNullOrBlank()"))
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
