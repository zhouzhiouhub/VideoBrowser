package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerPlaybackStateContractTest {
    @Test
    fun playerActivityDelegatesMutablePlaybackStateToStateHolder() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val playbackState = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerPlaybackState.kt"
        ).readText()

        assertTrue(playerActivity.contains("private val playbackState = NativePlayerPlaybackState()"))
        assertTrue(playerActivity.contains("fallbackPlaybackPosition = { playbackState.playbackPosition }"))
        assertTrue(playerActivity.contains("fallbackPlayWhenReady = { playbackState.playWhenReady }"))
        assertTrue(playerActivity.contains("currentMediaItemIndex = { playbackState.currentMediaItemIndex }"))
        assertTrue(playerActivity.contains("setCurrentMediaItemIndex = playbackState::setCurrentMediaItemIndex"))
        assertTrue(playerActivity.contains("setPlaybackPosition = playbackState::setPlaybackPosition"))
        assertTrue(playerActivity.contains("playbackState.restoreFrom(restoredState)"))
        assertTrue(playerActivity.contains("playbackState.updateFrom(it)"))
        assertTrue(playerActivity.contains("playbackState.setPlaybackPosition(positionMs)"))

        assertTrue(playbackState.contains("internal class NativePlayerPlaybackState"))
        assertTrue(playbackState.contains("var playbackPosition = 0L"))
        assertTrue(playbackState.contains("var playWhenReady = true"))
        assertTrue(playbackState.contains("var currentMediaItemIndex = 0"))
        assertTrue(playbackState.contains("fun restoreFrom(savedState: NativePlayerSavedState)"))
        assertTrue(playbackState.contains("fun updateFrom(exoPlayer: ExoPlayer)"))

        assertFalse(playerActivity.contains("private var playbackPosition"))
        assertFalse(playerActivity.contains("private var playWhenReady"))
        assertFalse(playerActivity.contains("private var currentMediaItemIndex"))
        assertFalse(playerActivity.contains("playbackPosition = it.currentPosition"))
        assertFalse(playerActivity.contains("playWhenReady = it.playWhenReady"))
    }

}
