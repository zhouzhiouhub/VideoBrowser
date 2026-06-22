package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerTransportControllerContractTest {
    @Test
    fun playerActivityDelegatesBasicTransportOperationsToController() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val transportController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerTransportController.kt"
        ).readText()
        val commandDispatcher = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerTransportController("))
        assertTrue(playerActivity.contains("nativePlayerTransportController::currentSeekPosition"))
        assertTrue(commandDispatcher.contains("transportController.seekBy(command.offsetMs)"))
        assertTrue(commandDispatcher.contains("transportController.seekTo(command.positionMs)"))
        assertTrue(commandDispatcher.contains("transportController.togglePlayPause()"))
        assertTrue(commandDispatcher.contains("transportController.play()"))
        assertTrue(commandDispatcher.contains("transportController.pause()"))
        assertFalse(playerActivity.contains("private fun seekPlayerBy"))
        assertFalse(playerActivity.contains("private fun seekPlayerTo"))
        assertFalse(playerActivity.contains("private fun togglePlayerPlayPause"))

        assertTrue(transportController.contains("fun seekBy(offsetMs: Long)"))
        assertTrue(transportController.contains("fun seekTo(positionMs: Long)"))
        assertTrue(transportController.contains("Media3Duration.boundedSeekPositionMs"))
        assertTrue(transportController.contains("fun currentSeekPosition()"))
        assertTrue(transportController.contains("Media3Duration.knownDurationMs"))
        assertTrue(transportController.contains("fun togglePlayPause()"))
        assertTrue(transportController.contains("fun play()"))
        assertTrue(transportController.contains("fun pause()"))
    }

}
