package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoDiagnosticsContractTest {
    @Test
    fun nativeVideoControllersUseSharedDiagnosticLogTag() {
        val fullscreenController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoController.kt"
        ).readText()
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        listOf(fullscreenController, playerActivity).forEach { source ->
            assertTrue(source.contains("private const val VIDEO_LOG_TAG = \"VideoBrowserVideo\""))
            assertTrue(source.contains("Log.d(VIDEO_LOG_TAG"))
        }
    }

    @Test
    fun fullscreenGestureOverlayDoesNotRenderSecondProgressBar() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertFalse(source.contains("seekProgressTrack"))
        assertFalse(source.contains("seekProgressFill"))
        assertFalse(source.contains("setupSeekProgress()"))
        assertFalse(source.contains("updateSeekProgress("))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
