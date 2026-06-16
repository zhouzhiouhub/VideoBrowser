package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Zoom Mode Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
