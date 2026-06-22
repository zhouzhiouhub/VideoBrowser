package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Native Video Zoom Wiring Contract Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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
        val savedState = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerSavedState.kt"
        ).readText()
        val controller = File(
            "src/main/java/com/example/videobrowser/video/NativePlayerVideoZoomController.kt"
        ).readText()

        assertTrue(source.contains("NativePlayerVideoZoomController("))
        assertTrue(source.contains("NativePlayerSavedState.restore("))
        assertTrue(savedState.contains("savedInstanceState.getString(STATE_VIDEO_ZOOM_MODE)"))
        assertTrue(savedState.contains("outState.putString(STATE_VIDEO_ZOOM_MODE, sessionState.zoomMode.name)"))
        assertTrue(controller.contains("private var videoZoomMode = VideoZoomMode.FIT"))
        assertTrue(controller.contains("playerView()?.resizeMode = videoZoomMode.resizeMode"))
        assertTrue(controller.contains("gestureOverlay()?.setVideoZoomMode(videoZoomMode)"))
        assertTrue(controller.contains("fun cycle(): VideoZoomMode"))
        assertTrue(source.contains("nativePlayerVideoZoomController.currentMode()"))
        assertTrue(source.contains("PlaybackCommand.CycleZoom"))
        assertTrue(source.contains("PlaybackCommand.CycleZoom -> nativePlayerVideoZoomController.cycle()"))

        assertFalse(source.contains("private var videoZoomMode = VideoZoomMode.FIT"))
        assertFalse(source.contains("playerView.resizeMode = videoZoomMode.resizeMode"))
        assertFalse(source.contains("gestureOverlay.setVideoZoomMode(videoZoomMode)"))
        assertFalse(source.contains("private fun applyVideoZoomMode"))
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
