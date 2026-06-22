package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativePlaybackCommandDispatcherContractTest {
    @Test
    fun playerActivityDelegatesPlaybackCommandBranchesToDispatcher() {
        val playerActivity = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val dispatcher = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()

        assertTrue(playerActivity.contains("NativePlaybackCommandDispatcher("))
        assertTrue(playerActivity.contains("return nativePlaybackCommandDispatcher.handle(command)"))
        assertFalse(playerActivity.contains("when (command)"))

        assertTrue(dispatcher.contains("internal class NativePlaybackCommandDispatcher"))
        assertTrue(dispatcher.contains("fun handle(command: PlaybackCommand): Any?"))
        assertTrue(dispatcher.contains("PlaybackCommand.Play -> transportController.play()"))
        assertTrue(dispatcher.contains("PlaybackCommand.TogglePlayPause -> transportController.togglePlayPause()"))
        assertTrue(dispatcher.contains("is PlaybackCommand.SetSpeed ->"))
        assertTrue(dispatcher.contains("playbackSpeedController.setSpeed(command.speed)"))
        assertTrue(dispatcher.contains("PlaybackCommand.ToggleRepeat ->"))
        assertTrue(dispatcher.contains("setPlaybackQueue(repeatModeController.cycle(playbackQueue()))"))
        assertTrue(dispatcher.contains("PlaybackCommand.ToggleShuffle -> queueController.toggleShuffleMode()"))
        assertTrue(dispatcher.contains("PlaybackCommand.CycleZoom -> videoZoomController.cycle()"))
        assertTrue(dispatcher.contains("trackSelectionDialogController.showDialog(command.trackType)"))
    }

}
