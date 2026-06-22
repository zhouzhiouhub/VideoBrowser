package com.example.videobrowser.video

import androidx.media3.ui.PlayerView

internal class NativePlayerVideoZoomController(
    private val playerView: () -> PlayerView?,
    private val gestureOverlay: () -> FullscreenVideoGestureOverlay?,
    private val wakePlayerControls: () -> Unit
) {
    private var videoZoomMode = VideoZoomMode.FIT

    fun currentMode(): VideoZoomMode {
        return videoZoomMode
    }

    fun setMode(mode: VideoZoomMode) {
        videoZoomMode = mode
        apply()
    }

    fun cycle(): VideoZoomMode {
        videoZoomMode = videoZoomMode.next()
        apply()
        wakePlayerControls()
        return videoZoomMode
    }

    fun apply() {
        playerView()?.resizeMode = videoZoomMode.resizeMode
        gestureOverlay()?.setVideoZoomMode(videoZoomMode)
    }
}
