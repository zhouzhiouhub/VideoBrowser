package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Video Zoom Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class NativeVideoZoomWiringContractTest {
    /**
     * 测试函数 `playerActivityAppliesAndPersistsVideoZoomMode`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `player Activity Applies And Persists Video Zoom Mode` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun playerActivityAppliesAndPersistsVideoZoomMode() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/PlayerActivity.kt"
        ).readText()

        assertTrue(source.contains("private var videoZoomMode = VideoZoomMode.FIT"))
        assertTrue(source.contains("savedInstanceState.getString(STATE_VIDEO_ZOOM_MODE)"))
        assertTrue(source.contains("outState.putString(STATE_VIDEO_ZOOM_MODE, sessionState.zoomMode.name)"))
        assertTrue(source.contains("playerView.resizeMode = videoZoomMode.resizeMode"))
        assertTrue(source.contains("PlaybackCommand.CycleZoom"))
        assertTrue(source.contains("private fun cycleVideoZoomMode(): VideoZoomMode"))
        assertTrue(source.contains("gestureOverlay.setVideoZoomMode(videoZoomMode)"))
    }

    /**
     * 测试函数 `overlayExposesVideoZoomButton`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `overlay Exposes Video Zoom Button` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun overlayExposesVideoZoomButton() {
        val source = File(
            "src/main/java/com/example/videobrowser/video/FullscreenVideoGestureOverlay.kt"
        ).readText()

        assertTrue(source.contains("var onVideoZoomRequested: (() -> VideoZoomMode)? = null"))
        assertTrue(source.contains("private val zoomButton = controlTextView()"))
        assertTrue(source.contains("fun setVideoZoomMode(mode: VideoZoomMode)"))
        assertTrue(source.contains("R.string.video_control_zoom_fit"))
        assertTrue(source.contains("R.string.video_control_zoom_stretch"))
        assertTrue(source.contains("R.string.video_control_zoom_crop"))
        assertTrue(source.contains("onVideoZoomRequested?.invoke()"))
    }

    /**
     * 测试函数 `videoZoomStringsExist`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `video Zoom Strings Exist` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun videoZoomStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_zoom_fit\""))
        assertTrue(strings.contains("name=\"video_control_zoom_stretch\""))
        assertTrue(strings.contains("name=\"video_control_zoom_crop\""))
    }
}
