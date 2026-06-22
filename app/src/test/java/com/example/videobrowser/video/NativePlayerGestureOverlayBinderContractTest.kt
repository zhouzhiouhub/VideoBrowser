package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlayerGestureOverlayBinderContractTest {
    @Test
    fun `player activity delegates gesture overlay wiring to binder`() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val binder = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlayerGestureOverlayBinder.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlayerGestureOverlayBinder("))
        assertTrue(playerActivity.contains("gestureOverlay = nativePlayerGestureOverlayBinder.attach()"))
        assertTrue(playerActivity.contains("handlePlaybackCommand = ::handlePlaybackCommand"))
        assertTrue(playerActivity.contains("hasMultipleQueueItems = { playbackQueue.hasMultipleItems }"))

        assertTrue(binder.contains("internal class NativePlayerGestureOverlayBinder"))
        assertTrue(binder.contains("fun attach(): FullscreenVideoGestureOverlay"))
        assertTrue(binder.contains("FullscreenVideoGestureOverlay(activity).apply"))
        assertTrue(binder.contains("onSeekBy = { offsetMs -> handlePlaybackCommand(PlaybackCommand.SeekBy(offsetMs)) }"))
        assertTrue(binder.contains("onPlaybackQueueRequested = { handlePlaybackCommand(PlaybackCommand.ShowQueue) }"))
        assertTrue(binder.contains("onRepeatModeRequested = {"))
        assertTrue(binder.contains("setQueueControlsVisible(hasMultipleQueueItems())"))
        assertTrue(binder.contains("playerRoot.addView("))

        assertFalse(playerActivity.contains("onSeekBy = { offsetMs ->"))
        assertFalse(playerActivity.contains("onPlaybackSpeedSelected = { speed ->"))
        assertFalse(playerActivity.contains("onPlaybackQueueRequested = {"))
        assertFalse(playerActivity.contains("onRepeatModeRequested = {"))
        assertFalse(playerActivity.contains("playerRoot.addView("))
    }

}
