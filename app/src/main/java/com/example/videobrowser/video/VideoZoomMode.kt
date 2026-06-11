package com.example.videobrowser.video

import androidx.media3.ui.AspectRatioFrameLayout

enum class VideoZoomMode(val resizeMode: Int) {
    FIT(AspectRatioFrameLayout.RESIZE_MODE_FIT),
    STRETCH(AspectRatioFrameLayout.RESIZE_MODE_FILL),
    CROP(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

    fun next(): VideoZoomMode {
        return when (this) {
            FIT -> STRETCH
            STRETCH -> CROP
            CROP -> FIT
        }
    }
}
