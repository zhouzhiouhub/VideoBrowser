package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

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
        val eventHandler = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureEventHandler.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoLongPressController("))
        assertTrue(eventHandler.contains("longPressController.scheduleIfSideZone(LONG_PRESS_TIMEOUT_MS)"))
        assertTrue(eventHandler.contains("longPressController.cancelScheduled()"))
        assertTrue(eventHandler.contains("longPressController.stopActive()"))
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
        assertFalse(overlay.contains("longPressController.scheduleIfSideZone(LONG_PRESS_TIMEOUT_MS)"))
    }

}
