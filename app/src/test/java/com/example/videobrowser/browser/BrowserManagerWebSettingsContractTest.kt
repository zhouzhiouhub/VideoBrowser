package com.example.videobrowser.browser

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserManagerWebSettingsContractTest {
    @Test
    fun browserManagerEnablesPinchZoomWithoutOverlayControls() {
        val browserManager = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowserManager.kt"
        ).readText()
        val readme = projectFile("README.md").readText()

        assertTrue(browserManager.contains("setSupportZoom(true)"))
        assertTrue(browserManager.contains("builtInZoomControls = true"))
        assertTrue(browserManager.contains("displayZoomControls = false"))
        assertTrue(readme.contains("双指缩放网页"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
