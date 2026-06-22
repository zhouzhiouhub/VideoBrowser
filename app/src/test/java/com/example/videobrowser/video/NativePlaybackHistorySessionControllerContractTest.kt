package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlaybackHistorySessionControllerContractTest {
    @Test
    fun playerActivityDelegatesHistoryIdentityAndSnapshotsToSessionController() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val sessionController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackHistorySessionController.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlaybackHistorySessionController("))
        assertTrue(playerActivity.contains("nativePlaybackHistorySessionController.restore()"))
        assertTrue(playerActivity.contains("nativePlaybackHistorySessionController.restorePositionForCurrentMedia()"))
        assertTrue(playerActivity.contains("nativePlaybackHistorySessionController.save(exoPlayer)"))
        assertFalse(playerActivity.contains("private fun playbackHistoryIdentity()"))
        assertFalse(playerActivity.contains("NativePlaybackHistorySnapshot("))

        assertTrue(sessionController.contains("private fun playbackHistoryIdentity(): String"))
        assertTrue(sessionController.contains("currentMediaItem()?.uri?.trim()"))
        assertTrue(sessionController.contains("fallbackMediaUri().trim()"))
        assertTrue(sessionController.contains("private fun currentMediaTitle(): String?"))
        assertTrue(sessionController.contains("currentMediaItem()?.title ?: fallbackMediaTitle()"))
        assertTrue(sessionController.contains("NativePlaybackHistorySnapshot("))
        assertTrue(sessionController.contains("Media3Duration.durationOrZero(exoPlayer.duration)"))
    }

}
