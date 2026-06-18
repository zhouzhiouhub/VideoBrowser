package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Track Selection Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import java.io.File
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
        val source = projectFile(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onTrackSelectionRequested: (() -> Unit)? = null"))
        assertTrue(source.contains("private val trackButton"))
        assertTrue(source.contains("trackButton.setOnClickListener"))
        assertTrue(source.contains("onTrackSelectionRequested?.invoke()"))
        assertTrue(source.contains("R.string.video_control_tracks"))
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
        val options = projectFile(
            "src/main/java/com/example/videobrowser/video/NativeTrackSelectionOptions.kt"
        ).readText()

        assertTrue(source.contains("NativeTrackSelectionDialogController("))
        assertTrue(source.contains("trackSelectionDialogController.showMenu()"))
        assertTrue(source.contains("trackSelectionDialogController.showDialog(command.trackType)"))
        assertTrue(dialogController.contains("TrackSelectionDialogBuilder"))
        assertTrue(dialogController.contains("NativeTrackSelectionOptions.menuOptions()"))
        assertTrue(dialogController.contains("NativeTrackSelectionOptions.optionFor(trackType)"))
        assertTrue(options.contains("PlaybackTrackType.AUDIO"))
        assertTrue(options.contains("PlaybackTrackType.SUBTITLE"))
        assertTrue(options.contains("C.TRACK_TYPE_AUDIO"))
        assertTrue(options.contains("C.TRACK_TYPE_TEXT"))
        assertTrue(source.contains("PlaybackCommand.ShowTrackSelection"))
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

    /**
     * 测试函数 `projectFile`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `project File` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param path 参数类型为 `String`，表示函数执行 `path` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun projectFile(path: String): File {
        val workingDirectory = File("").absoluteFile
        return listOf(
            File(workingDirectory, path),
            File(workingDirectory, "app/$path")
        ).first { it.exists() }
    }
}
