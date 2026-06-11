package com.example.videobrowser.video

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
        assertTrue(source.contains("outState.putString(STATE_VIDEO_ZOOM_MODE, videoZoomMode.name)"))
        assertTrue(source.contains("playerView.resizeMode = videoZoomMode.resizeMode"))
        assertTrue(source.contains("onVideoZoomRequested = ::cycleVideoZoomMode"))
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
