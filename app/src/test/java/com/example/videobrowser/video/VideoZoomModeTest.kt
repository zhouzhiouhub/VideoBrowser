package com.example.videobrowser.video

import androidx.media3.ui.AspectRatioFrameLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoZoomModeTest {
    @Test
    fun nextCyclesThroughFitStretchAndCrop() {
        assertEquals(VideoZoomMode.STRETCH, VideoZoomMode.FIT.next())
        assertEquals(VideoZoomMode.CROP, VideoZoomMode.STRETCH.next())
        assertEquals(VideoZoomMode.FIT, VideoZoomMode.CROP.next())
    }

    @Test
    fun modesMapToMedia3ResizeModes() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, VideoZoomMode.FIT.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, VideoZoomMode.STRETCH.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, VideoZoomMode.CROP.resizeMode)
    }
}
