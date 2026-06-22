package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoGestureEventHandlerContractTest {
    @Test
    fun `gesture overlay delegates motion event state machine to handler`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val handler = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureEventHandler.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoGestureEventHandler("))
        assertTrue(overlay.contains("gestureEventHandler.handle(event)"))
        assertTrue(overlay.contains("gestureEventHandler.cancelActiveHorizontalSeek()"))

        assertTrue(handler.contains("fun handle(event: MotionEvent): Boolean"))
        assertTrue(handler.contains("MotionEvent.ACTION_DOWN"))
        assertTrue(handler.contains("MotionEvent.ACTION_MOVE"))
        assertTrue(handler.contains("MotionEvent.ACTION_UP"))
        assertTrue(handler.contains("MotionEvent.ACTION_CANCEL"))
        assertTrue(handler.contains("touchSession.beginGestureDown("))
        assertTrue(handler.contains("longPressController.scheduleIfSideZone(LONG_PRESS_TIMEOUT_MS)"))
        assertTrue(handler.contains("beginHorizontalSeek(deltaX)"))
        assertTrue(handler.contains("updateHorizontalSeek(deltaX)"))
        assertTrue(handler.contains("finishHorizontalSeek(commit = true)"))
        assertTrue(handler.contains("finishHorizontalSeek(commit = false)"))
        assertTrue(handler.contains("handleTap(event.x, event.eventTime)"))

        assertFalse(overlay.contains("private fun handleGestureEvent"))
        assertFalse(overlay.contains("private fun beginHorizontalSeek"))
        assertFalse(overlay.contains("private fun updateHorizontalSeek"))
        assertFalse(overlay.contains("private fun finishHorizontalSeek"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
