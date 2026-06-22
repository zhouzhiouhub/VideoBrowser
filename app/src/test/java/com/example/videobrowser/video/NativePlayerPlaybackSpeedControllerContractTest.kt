package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerPlaybackSpeedControllerContractTest {
    @Test
    fun `player activity delegates native playback speed state to controller`() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val playerInitializer = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerInitializer.kt"
        ).readText()
        val controller = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerPlaybackSpeedController.kt"
        ).readText()
        val commandDispatcher = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerPlaybackSpeedController("))
        assertTrue(playerActivity.contains("nativePlayerPlaybackSpeedController.restoreSpeed("))
        assertTrue(commandDispatcher.contains("playbackSpeedController.setSpeed(command.speed)"))
        assertTrue(playerActivity.contains("nativePlayerPlaybackSpeedController.currentSpeed()"))
        assertTrue(playerActivity.contains("applyPlaybackSpeed = nativePlayerPlaybackSpeedController::applyToPlayer"))
        assertTrue(playerInitializer.contains("applyPlaybackSpeed(exoPlayer)"))

        assertTrue(controller.contains("private var selectedPlaybackSpeed = DEFAULT_PLAYBACK_SPEED"))
        assertTrue(controller.contains("PlaybackSpeedNormalizer::normalize"))
        assertTrue(controller.contains("saveDefaultVideoSpeed(selectedPlaybackSpeed)"))
        assertTrue(controller.contains("player.setPlaybackSpeed(selectedPlaybackSpeed)"))
        assertTrue(controller.contains("gestureOverlay()?.setPlaybackSpeed(selectedPlaybackSpeed)"))

        assertFalse(playerActivity.contains("private var selectedPlaybackSpeed"))
        assertFalse(playerActivity.contains("private fun setPlayerPlaybackSpeed"))
        assertFalse(playerActivity.contains("PlaybackSpeedNormalizer.normalize(speed)"))
        assertFalse(playerActivity.contains("settingsManager.setDefaultVideoSpeed(selectedPlaybackSpeed)"))
    }

}
