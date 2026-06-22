package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Playback Command Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class NativePlaybackCommandWiringContractTest {
    /**
     * 测试函数 `playerActivityRoutesOverlayCallbacksThroughPlaybackCommands`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Routes Overlay Callbacks Through Playback Commands` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityRoutesOverlayCallbacksThroughPlaybackCommands() {
        val source = File("src/main/java/com/example/videobrowser/video/PlayerActivity.kt").readText()
        val commandDispatcher = File(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()
        val gestureOverlayBinder = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerGestureOverlayBinder.kt"
        ).readText()
        val trackOptions = File(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionOptions.kt"
        ).readText()

        assertTrue(source.contains("handlePlaybackCommand(command: PlaybackCommand)"))
        assertTrue(source.contains("nativePlaybackCommandDispatcher.handle(command)"))
        assertTrue(source.contains("NativePlaybackCommandDispatcher("))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.SeekBy(offsetMs)"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.SeekTo(positionMs)"))
        assertTrue(commandDispatcher.contains("PlaybackCommand.TogglePlayPause"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.SetSpeed(speed)"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.Previous"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.Next"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.ToggleRepeat"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.ShowQueue"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.CycleZoom"))
        assertTrue(gestureOverlayBinder.contains("PlaybackCommand.ShowTrackSelection"))
        assertTrue(source.contains("handlePlaybackCommand(PlaybackCommand.SelectTrack(trackType))"))
        assertTrue(commandDispatcher.contains("trackSelectionDialogController.showDialog(command.trackType)"))
        assertFalse(source.contains("when (command)"))
        assertTrue(trackOptions.contains("PlaybackTrackType.AUDIO"))
        assertTrue(trackOptions.contains("PlaybackTrackType.SUBTITLE"))
    }

    /**
     * 测试函数 `playerActivitySnapshotsNativePlaybackSessionState`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Snapshots Native Playback Session State` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivitySnapshotsNativePlaybackSessionState() {
        val source = File("src/main/java/com/example/videobrowser/video/PlayerActivity.kt").readText()
        val duration = File("src/main/java/com/example/videobrowser/video/Media3Duration.kt").readText()
        val transportController = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerTransportController.kt"
        ).readText()
        val historySessionController = File(
            "src/main/java/com/example/videobrowser/video/NativePlaybackHistorySessionController.kt"
        ).readText()

        assertTrue(source.contains("currentPlaybackSessionState()"))
        assertTrue(source.contains("PlaybackSessionState.fromQueue("))
        assertTrue(source.contains("val sessionState = currentPlaybackSessionState()"))
        assertTrue(source.contains("Media3Duration::knownDurationMs"))
        assertTrue(transportController.contains("Media3Duration.knownDurationMs"))
        assertTrue(transportController.contains("Media3Duration.boundedSeekPositionMs"))
        assertTrue(historySessionController.contains("Media3Duration.durationOrZero"))
        assertTrue(duration.contains("C.TIME_UNSET"))
        assertFalse(source.contains("C.TIME_UNSET"))
    }
}
