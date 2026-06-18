package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextWhitespaceNormalizerContractTest {
    @Test
    fun `text cleanup callers share whitespace normalization`() {
        val shortcutStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/CustomShortcutStore.kt"
        ).readText()
        val elementRuleStore = projectFile(
            "src/main/java/com/example/videobrowser/settings/UserElementHideRuleStore.kt"
        ).readText()
        val downloadSafetyPolicy = projectFile(
            "src/main/java/com/example/videobrowser/download/DownloadSafetyPolicy.kt"
        ).readText()
        val playbackHistoryRepository = projectFile(
            "src/main/java/com/example/videobrowser/video/PlaybackHistoryRepository.kt"
        ).readText()
        val savedPageRepository = projectFile(
            "src/main/java/com/example/videobrowser/storage/SavedPageRepository.kt"
        ).readText()
        val nativeBridge = projectFile(
            "src/main/java/com/example/videobrowser/browser/VideoBrowserNativeBridge.kt"
        ).readText()

        listOf(
            shortcutStore,
            elementRuleStore,
            downloadSafetyPolicy,
            playbackHistoryRepository,
            savedPageRepository,
            nativeBridge
        ).forEach { source ->
            assertTrue(source.contains("TextWhitespaceNormalizer"))
            assertFalse(source.contains("Regex(\"\\\\s+\")"))
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
