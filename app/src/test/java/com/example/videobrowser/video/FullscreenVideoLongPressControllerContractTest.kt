package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoLongPressControllerContractTest {
    @Test
    fun `gesture overlay delegates long press flow to controller`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoLongPressController.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoLongPressController("))
        assertTrue(overlay.contains("longPressController.scheduleIfSideZone(LONG_PRESS_TIMEOUT_MS)"))
        assertTrue(overlay.contains("longPressController.cancelScheduled()"))
        assertTrue(overlay.contains("longPressController.stopActive()"))

        assertTrue(controller.contains("private val longPressRunnable = Runnable"))
        assertTrue(controller.contains("fun scheduleIfSideZone(delayMs: Long)"))
        assertTrue(controller.contains("fun cancelScheduled()"))
        assertTrue(controller.contains("fun trigger()"))
        assertTrue(controller.contains("fun stopActive()"))
        assertTrue(controller.contains("touchSession.startLongPress()"))
        assertTrue(controller.contains("touchSession.stopLongPress()"))
        assertTrue(controller.contains("VideoSpeedOptions.longPressSpeed"))
        assertTrue(controller.contains("requestDirectionalLongPressStart(direction)"))
        assertTrue(controller.contains("requestDirectionalLongPressEnd()"))

        assertFalse(overlay.contains("private val longPressRunnable"))
        assertFalse(overlay.contains("private fun triggerLongPress"))
        assertFalse(overlay.contains("private fun stopLongPress"))
        assertFalse(overlay.contains("VideoSpeedOptions.longPressSpeed"))
    }

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
