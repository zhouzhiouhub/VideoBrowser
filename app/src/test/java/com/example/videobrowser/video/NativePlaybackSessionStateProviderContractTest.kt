package com.example.videobrowser.video

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlaybackSessionStateProviderContractTest {
    @Test
    fun playerActivityDelegatesPlaybackSessionStateSnapshotToProvider() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val provider = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackSessionStateProvider.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlaybackSessionStateProvider("))
        assertTrue(playerActivity.contains("nativePlaybackSessionStateProvider.currentState()"))
        assertFalse(playerActivity.contains("private fun currentPlaybackSessionState("))
        assertFalse(playerActivity.contains("PlaybackSessionState.fromQueue("))

        assertTrue(provider.contains("internal class NativePlaybackSessionStateProvider"))
        assertTrue(provider.contains("fun currentState(): PlaybackSessionState"))
        assertTrue(provider.contains("Media3Duration::knownDurationMs"))
        assertTrue(provider.contains("PlaybackSessionState.fromQueue("))
        assertTrue(provider.contains("positionMs = exoPlayer?.currentPosition ?: fallbackPlaybackPosition()"))
        assertTrue(provider.contains("playWhenReady = exoPlayer?.playWhenReady ?: fallbackPlayWhenReady()"))
        assertTrue(provider.contains("zoomMode = currentVideoZoomMode()"))
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
