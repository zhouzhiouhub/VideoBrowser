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

    @Test
    fun videoZoomStringsExist() {
        val strings = File("src/main/res/values/strings.xml").readText()

        assertTrue(strings.contains("name=\"video_control_zoom_fit\""))
        assertTrue(strings.contains("name=\"video_control_zoom_stretch\""))
        assertTrue(strings.contains("name=\"video_control_zoom_crop\""))
    }
}
