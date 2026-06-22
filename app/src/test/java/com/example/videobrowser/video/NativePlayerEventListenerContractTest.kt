package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerEventListenerContractTest {
    @Test
    fun playerActivityDelegatesExoPlayerEventsToNativePlayerEventListener() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val eventListener = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerEventListener.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerEventListener("))
        assertTrue(playerActivity.contains("retryPlaybackWithoutVideoEffects = ::retryPlaybackWithoutVideoEffects"))
        assertTrue(playerActivity.contains("savePlaybackHistory = { savePlaybackHistory(exoPlayer) }"))
        assertTrue(playerActivity.contains("handleMediaItemTransition(exoPlayer.currentMediaItemIndex)"))
        assertFalse(playerActivity.contains("object : Player.Listener"))
        assertFalse(playerActivity.contains("override fun onPlayerError"))

        assertTrue(eventListener.contains(": Player.Listener"))
        assertTrue(eventListener.contains("override fun onPlayerError(error: PlaybackException)"))
        assertTrue(eventListener.contains("retryPlaybackWithoutVideoEffects()"))
        assertTrue(eventListener.contains("showPlaybackFailed()"))
        assertTrue(eventListener.contains("override fun onPlayWhenReadyChanged"))
        assertTrue(eventListener.contains("savePlaybackHistory()"))
        assertTrue(eventListener.contains("override fun onPlaybackStateChanged"))
        assertTrue(eventListener.contains("Player.STATE_READY"))
        assertTrue(eventListener.contains("Player.STATE_ENDED"))
        assertTrue(eventListener.contains("override fun onMediaItemTransition"))
        assertTrue(eventListener.contains("mediaItemTransitioned()"))
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
