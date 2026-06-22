package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoSideTapSeekControllerContractTest {
    @Test
    fun `gesture overlay delegates side tap seek state to controller`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoSideTapSeekController.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoSideTapSeekController("))
        assertTrue(overlay.contains("sideTapSeekController.registerTap(zone, eventTime)"))
        assertTrue(overlay.contains("sideTapSeekController.clearPendingTap()"))
        assertTrue(overlay.contains("sideTapSeekController.clearAll()"))

        assertTrue(controller.contains("private var pendingTapZone = VideoGestureScreenZone.NONE"))
        assertTrue(controller.contains("private var pendingTapTime = 0L"))
        assertTrue(controller.contains("private var seekAccumulatorDirection = 0"))
        assertTrue(controller.contains("private var seekAccumulatorCount = 0"))
        assertTrue(controller.contains("private val clearPendingTapRunnable = Runnable"))
        assertTrue(controller.contains("private val clearSeekAccumulatorRunnable = Runnable"))
        assertTrue(controller.contains("fun registerTap(zone: VideoGestureScreenZone, eventTime: Long)"))
        assertTrue(controller.contains("VideoGestureFeedbackFormatter.formatSeekSeconds(seconds)"))

        assertFalse(overlay.contains("private var pendingTapZone"))
        assertFalse(overlay.contains("private var pendingTapTime"))
        assertFalse(overlay.contains("private var seekAccumulatorDirection"))
        assertFalse(overlay.contains("private var seekAccumulatorCount"))
        assertFalse(overlay.contains("private val clearPendingTapRunnable"))
        assertFalse(overlay.contains("private val clearSeekAccumulatorRunnable"))
        assertFalse(overlay.contains("private fun registerSideTap"))
        assertFalse(overlay.contains("private fun handleDoubleTap"))
        assertFalse(overlay.contains("private const val SEEK_STEP_MS"))
        assertFalse(overlay.contains("private const val SEEK_STEP_SECONDS"))
        assertFalse(overlay.contains("private const val DOUBLE_TAP_TIMEOUT_MS"))
        assertFalse(overlay.contains("private const val SEEK_ACCUMULATE_RESET_MS"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
