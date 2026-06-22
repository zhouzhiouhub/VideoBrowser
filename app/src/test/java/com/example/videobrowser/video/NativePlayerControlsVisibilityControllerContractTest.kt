package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerControlsVisibilityControllerContractTest {
    @Test
    fun `player activity delegates native control visibility to controller`() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerControlsVisibilityController.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerControlsVisibilityController("))
        assertTrue(playerActivity.contains("playerControlsVisibilityController.applyDefaultHideTimeout()"))
        assertTrue(playerActivity.contains("playerControlsVisibilityController.wakeControls()"))
        assertTrue(playerActivity.contains("playerControlsVisibilityController.areControlsVisible()"))

        assertTrue(controller.contains("playerView.setControllerShowTimeoutMs(CONTROLS_HIDE_DELAY_MS)"))
        assertTrue(controller.contains("playerView.showController()"))
        assertTrue(controller.contains("playerView.isControllerFullyVisible"))
        assertTrue(controller.contains("Player.STATE_IDLE"))
        assertTrue(controller.contains("Player.STATE_ENDED"))
        assertTrue(controller.contains("const val CONTROLS_HIDE_DELAY_MS = 3000"))
        assertTrue(controller.contains("\"event=native-wake-controls keepVisible=\$keepVisible \""))

        assertFalse(playerActivity.contains("private fun shouldKeepPlayerControlsVisible()"))
        assertFalse(playerActivity.contains("playerView.isControllerFullyVisible"))
        assertFalse(playerActivity.contains("playerView.setControllerShowTimeoutMs("))
        assertFalse(playerActivity.contains("playerView.showController()"))
        assertFalse(playerActivity.contains("private const val CONTROLS_HIDE_DELAY_MS"))
    }

}
