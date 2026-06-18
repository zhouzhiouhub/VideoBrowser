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
    /**
     * 测试函数 `playerActivityRoutesOverlayCallbacksThroughPlaybackCommands`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Routes Overlay Callbacks Through Playback Commands` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityRoutesOverlayCallbacksThroughPlaybackCommands() {
        val source = File("src/main/java/com/example/videobrowser/video/PlayerActivity.kt").readText()
        val trackOptions = File(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionOptions.kt"
        ).readText()

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
        assertTrue(source.contains("handlePlaybackCommand(PlaybackCommand.SelectTrack(trackType))"))
        assertTrue(source.contains("trackSelectionDialogController.showDialog(command.trackType)"))
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

        assertTrue(source.contains("currentPlaybackSessionState()"))
        assertTrue(source.contains("PlaybackSessionState.fromQueue("))
        assertTrue(source.contains("val sessionState = currentPlaybackSessionState()"))
    }
}
