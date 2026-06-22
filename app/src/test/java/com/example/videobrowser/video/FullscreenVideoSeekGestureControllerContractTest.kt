package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoSeekGestureControllerContractTest {
    @Test
    fun `gesture overlay delegates horizontal seek session state to controller`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoSeekGestureController.kt"
        ).readText()
        val eventHandler = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureEventHandler.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoSeekGestureController("))
        assertTrue(eventHandler.contains("seekGestureController.begin(deltaX, viewWidth())"))
        assertTrue(eventHandler.contains("seekGestureController.update(deltaX, viewWidth())"))
        assertTrue(eventHandler.contains("seekGestureController.finish(commit)"))

        assertTrue(controller.contains("private var startPositionMs: Long? = null"))
        assertTrue(controller.contains("private var durationMs: Long? = null"))
        assertTrue(controller.contains("private var pendingOffsetMs = 0L"))
        assertTrue(controller.contains("private var pendingTargetMs: Long? = null"))
        assertTrue(controller.contains("VideoSeekDragCalculator.offsetForDrag("))
        assertTrue(controller.contains("VideoSeekDragCalculator.targetForDrag("))
        assertTrue(controller.contains("VideoGestureFeedbackFormatter.formatSeekPreview("))
        assertTrue(controller.contains("pendingTargetMs?.let(seekTo) ?: seekBy(pendingOffsetMs)"))

        assertFalse(overlay.contains("private var seekStartPositionMs"))
        assertFalse(overlay.contains("private var seekDurationMs"))
        assertFalse(overlay.contains("private var pendingHorizontalSeekMs"))
        assertFalse(overlay.contains("private var pendingSeekTargetMs"))
        assertFalse(overlay.contains("VideoSeekDragCalculator.offsetForDrag("))
        assertFalse(overlay.contains("VideoSeekDragCalculator.targetForDrag("))
        assertFalse(overlay.contains("VideoGestureFeedbackFormatter.formatSeekPreview("))
        assertFalse(overlay.contains("seekGestureController.begin(deltaX"))
        assertFalse(overlay.contains("seekGestureController.update(deltaX"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
