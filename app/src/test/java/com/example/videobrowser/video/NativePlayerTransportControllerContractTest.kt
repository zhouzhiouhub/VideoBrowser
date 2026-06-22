package com.example.videobrowser.video

import java.io.File
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

        assertTrue(playerActivity.contains("NativePlayerTransportController("))
        assertTrue(playerActivity.contains("nativePlayerTransportController::currentSeekPosition"))
        assertTrue(playerActivity.contains("nativePlayerTransportController.seekBy(command.offsetMs)"))
        assertTrue(playerActivity.contains("nativePlayerTransportController.seekTo(command.positionMs)"))
        assertTrue(playerActivity.contains("nativePlayerTransportController.togglePlayPause()"))
        assertTrue(playerActivity.contains("nativePlayerTransportController.play()"))
        assertTrue(playerActivity.contains("nativePlayerTransportController.pause()"))
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

    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOfNotNull(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path"),
            workingDirectory.parentFile?.let { parent -> File(parent, path) }
        ).first { it.exists() }
    }
}
