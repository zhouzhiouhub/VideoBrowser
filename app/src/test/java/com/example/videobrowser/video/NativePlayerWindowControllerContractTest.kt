package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerWindowControllerContractTest {
    @Test
    fun `player activity delegates native window controls`() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val windowController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerWindowController.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerWindowController(this)"))
        assertTrue(playerActivity.contains("nativePlayerWindowController.applyOrientation(isLandscape)"))
        assertTrue(playerActivity.contains("nativePlayerWindowController.hideSystemBars()"))
        assertFalse(playerActivity.contains("WindowInsetsControllerCompat"))
        assertTrue(windowController.contains("WindowCompat.setDecorFitsSystemWindows"))
        assertTrue(windowController.contains("WindowInsetsCompat.Type.systemBars()"))
        assertTrue(windowController.contains("BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
