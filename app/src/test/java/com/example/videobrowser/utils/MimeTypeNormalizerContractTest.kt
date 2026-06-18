package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MimeTypeNormalizerContractTest {
    @Test
    fun `mime type callers share normalization`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/download/DownloadCategory.kt"),
            projectFile("src/main/java/com/example/videobrowser/utils/MediaUrlUtils.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/PlayableMediaItemMedia3Converter.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/LocalSubtitleMatcher.kt"),
            projectFile("src/main/java/com/example/videobrowser/browser/LocalWebArchivePolicy.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("MimeTypeNormalizer"))
            assertFalse(source.contains("substringBefore(';')"))
            assertFalse(source.contains("startsWith(\"video/\")"))
            assertFalse(source.contains("startsWith(\"image/\")"))
            assertFalse(source.contains("startsWith(\"audio/\")"))
            assertFalse(source.contains("startsWith(\"text/\")"))
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
