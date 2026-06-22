package com.example.videobrowser.video

import com.example.videobrowser.testutil.projectFile

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Track Selection Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NativeTrackSelectionContractTest {
    /**
     * 测试函数 `fullscreenOverlayProvidesTrackSelectionButton`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `fullscreen Overlay Provides Track Selection Button` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun fullscreenOverlayProvidesTrackSelectionButton() {
        val overlay = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()
        val controlsController = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoControlsGroupController.kt"
        ).readText()

        assertTrue(overlay.contains("var onTrackSelectionRequested: (() -> Unit)? = null"))
        assertTrue(overlay.contains("FullscreenVideoControlsGroupController("))
        assertTrue(overlay.contains("requestTrackSelection = { onTrackSelectionRequested?.invoke() }"))
        assertFalse(overlay.contains("trackButton.setOnClickListener"))
        assertTrue(controlsController.contains("private val trackButton: TextView"))
        assertTrue(controlsController.contains("private fun setupTrackButton()"))
        assertTrue(controlsController.contains("trackButton.setOnClickListener"))
        assertTrue(controlsController.contains("requestTrackSelection()"))
        assertTrue(controlsController.contains("R.string.video_control_tracks"))
    }

    /**
     * 测试函数 `playerActivityShowsMedia3TrackSelectionDialogsForAudioAndSubtitles`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Shows Media3 Track Selection Dialogs For Audio And Subtitles` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityShowsMedia3TrackSelectionDialogsForAudioAndSubtitles() {
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()
        val dialogController = projectFile(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionDialogController.kt"
        ).readText()
        val commandDispatcher = projectFile(
            "src/main/java/com/example/videobrowser/video/NativePlaybackCommandDispatcher.kt"
        ).readText()
        val options = projectFile(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionOptions.kt"
        ).readText()

        assertTrue(source.contains("NativeTrackSelectionDialogController("))
        assertTrue(commandDispatcher.contains("trackSelectionDialogController.showMenu()"))
        assertTrue(commandDispatcher.contains("trackSelectionDialogController.showDialog(command.trackType)"))
        assertTrue(dialogController.contains("TrackSelectionDialogBuilder"))
        assertTrue(dialogController.contains("NativeTrackSelectionOptions.menuOptions()"))
        assertTrue(dialogController.contains("NativeTrackSelectionOptions.optionFor(trackType)"))
        assertTrue(options.contains("PlaybackTrackType.AUDIO"))
        assertTrue(options.contains("PlaybackTrackType.SUBTITLE"))
        assertTrue(options.contains("C.TRACK_TYPE_AUDIO"))
        assertTrue(options.contains("C.TRACK_TYPE_TEXT"))
        assertTrue(commandDispatcher.contains("PlaybackCommand.ShowTrackSelection"))
        assertFalse(source.contains("TrackSelectionDialogBuilder"))
        assertFalse(source.contains("C.TRACK_TYPE_AUDIO"))
        assertFalse(source.contains("C.TRACK_TYPE_TEXT"))
    }

    /**
     * 测试函数 `trackSelectionStringsAreDefined`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `track Selection Strings Are Defined` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun trackSelectionStringsAreDefined() {
        val strings = projectFile("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_tracks\""))
        assertTrue(strings.contains("name=\"video_track_audio\""))
        assertTrue(strings.contains("name=\"video_track_subtitles\""))
        assertTrue(strings.contains("name=\"toast_video_tracks_unavailable\""))
    }

}
