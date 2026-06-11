package com.example.videobrowser.video

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NativePlaybackCommandWiringContractTest {
    @Test
    fun playerActivityRoutesOverlayCallbacksThroughPlaybackCommands() {
        val source = File("src/main/java/com/example/videobrowser/video/PlayerActivity.kt").readText()

        assertTrue(source.contains("handlePlaybackCommand(command: PlaybackCommand)"))
        assertTrue(source.contains("PlaybackCommand.SeekBy(offsetMs)"))
        assertTrue(source.contains("PlaybackCommand.SeekTo(positionMs)"))
        assertTrue(source.contains("PlaybackCommand.TogglePlayPause"))
        assertTrue(source.contains("PlaybackCommand.SetSpeed(speed)"))
        assertTrue(source.contains("PlaybackCommand.Previous"))
        assertTrue(source.contains("PlaybackCommand.Next"))
        assertTrue(source.contains("PlaybackCommand.ToggleRepeat"))
        assertTrue(source.contains("PlaybackCommand.ShowQueue"))
        assertTrue(source.contains("PlaybackCommand.CycleZoom"))
        assertTrue(source.contains("PlaybackCommand.ShowTrackSelection"))
        assertTrue(source.contains("PlaybackCommand.SelectTrack(PlaybackTrackType.AUDIO)"))
        assertTrue(source.contains("PlaybackCommand.SelectTrack(PlaybackTrackType.SUBTITLE)"))
    }

    @Test
    fun playerActivitySnapshotsNativePlaybackSessionState() {
        val source = File("src/main/java/com/example/videobrowser/video/PlayerActivity.kt").readText()

        assertTrue(source.contains("currentPlaybackSessionState()"))
        assertTrue(source.contains("PlaybackSessionState.fromQueue("))
        assertTrue(source.contains("val sessionState = currentPlaybackSessionState()"))
    }
}
