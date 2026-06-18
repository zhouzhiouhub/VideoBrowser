package com.example.videobrowser.utils

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserDrawableFactoryContractTest {
    @Test
    fun `drawable background creation is centralized`() {
        val factory = projectFile(
            "src/main/java/com/example/videobrowser/utils/BrowserDrawableFactory.kt"
        ).readText()
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val functionCenterSurface = projectFile(
            "src/main/java/com/example/videobrowser/functioncenter/FunctionCenterSurfaceFactory.kt"
        ).readText()
        val searchProviderController = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderController.kt"
        ).readText()
        val searchProviderItemFactory = projectFile(
            "src/main/java/com/example/videobrowser/browser/search/SearchProviderItemFactory.kt"
        ).readText()
        val browsingModeTheme = projectFile(
            "src/main/java/com/example/videobrowser/browser/BrowsingModeThemeController.kt"
        ).readText()

        assertTrue(factory.contains("object BrowserDrawableFactory"))
        assertTrue(factory.contains("fun roundedBackground("))
        assertTrue(factory.contains("fun topRoundedBackground("))
        assertTrue(factory.contains("fun circleBackground("))
        assertFalse(overlay.contains("private fun roundedBackground("))
        assertTrue(overlay.contains("BrowserDrawableFactory.roundedBackground"))
        assertFalse(functionCenterSurface.contains("GradientDrawable().apply"))
        assertTrue(functionCenterSurface.contains("BrowserDrawableFactory.roundedBackground"))
        assertTrue(functionCenterSurface.contains("BrowserDrawableFactory.topRoundedBackground"))
        assertTrue(searchProviderController.contains("BrowserDrawableFactory.circleBackground"))
        assertFalse(searchProviderItemFactory.contains("private fun createCircleBackground("))
        assertTrue(searchProviderItemFactory.contains("BrowserDrawableFactory.circleBackground"))
        assertTrue(browsingModeTheme.contains("BrowserDrawableFactory.roundedBackground"))
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
