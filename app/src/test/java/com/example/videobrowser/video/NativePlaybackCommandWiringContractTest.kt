package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Playback Command Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
