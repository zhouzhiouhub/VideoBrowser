package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DensityPixelConverterContractTest {
    @Test
    fun `activity and fullscreen overlay share density conversion`() {
        val sources = listOf(
            projectFile("src/main/java/com/example/videobrowser/MainActivity.kt"),
            projectFile("src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt")
        ).map { file -> file.readText() }

        sources.forEach { source ->
            assertTrue(source.contains("DensityPixelConverter."))
            assertFalse(source.contains("resources.displayMetrics.density"))
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
