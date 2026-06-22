package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FullscreenVideoSystemGestureControllerContractTest {
    @Test
    fun `gesture overlay delegates brightness and volume system access to controller`() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoSystemGestureController.kt"
        ).readText()
        val gestureMath = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureMath.kt"
        ).readText()
        val eventHandler = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureEventHandler.kt"
        ).readText()

        assertTrue(overlay.contains("FullscreenVideoSystemGestureController("))
        assertTrue(overlay.contains("systemGestureController.captureWindowBrightness()"))
        assertTrue(overlay.contains("systemGestureController.restoreWindowBrightness()"))
        assertTrue(eventHandler.contains("systemGestureController.currentWindowBrightness()"))
        assertTrue(eventHandler.contains("systemGestureController.currentStreamVolume()"))
        assertTrue(eventHandler.contains("systemGestureController.updateBrightness("))
        assertTrue(eventHandler.contains("systemGestureController.updateVolume("))

        assertTrue(controller.contains("activity.getSystemService(Context.AUDIO_SERVICE) as AudioManager"))
        assertTrue(controller.contains("Settings.System.getInt("))
        assertTrue(controller.contains("FullscreenVideoGestureMath.brightnessForDrag("))
        assertTrue(controller.contains("FullscreenVideoGestureMath.volumeForDrag("))
        assertTrue(controller.contains("VideoGestureFeedbackFormatter.formatBrightness(brightness)"))
        assertTrue(controller.contains("VideoGestureFeedbackFormatter.formatVolume(nextVolume, minVolume, maxVolume)"))
        assertTrue(controller.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.P"))
        assertTrue(gestureMath.contains("const val DEFAULT_BRIGHTNESS = 0.5f"))

        assertFalse(overlay.contains("import android.media.AudioManager"))
        assertFalse(overlay.contains("import android.provider.Settings"))
        assertFalse(overlay.contains("import android.os.Build"))
        assertFalse(overlay.contains("activity.getSystemService(Context.AUDIO_SERVICE)"))
        assertFalse(overlay.contains("Settings.System.getInt("))
        assertFalse(overlay.contains("private fun streamMinVolume()"))
        assertFalse(overlay.contains("private fun updateBrightness("))
        assertFalse(overlay.contains("private fun updateVolume("))
        assertFalse(overlay.contains("private var savedWindowBrightness"))
        assertFalse(overlay.contains("private const val DEFAULT_BRIGHTNESS"))
        assertFalse(overlay.contains("systemGestureController.updateBrightness("))
        assertFalse(overlay.contains("systemGestureController.updateVolume("))
        assertFalse(controller.contains("private const val DEFAULT_BRIGHTNESS"))
    }

}
