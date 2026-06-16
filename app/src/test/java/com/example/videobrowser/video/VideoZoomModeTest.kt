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
    /**
     * 测试函数 `nextCyclesThroughFitStretchAndCrop`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `next Cycles Through Fit Stretch And Crop` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun nextCyclesThroughFitStretchAndCrop() {
        assertEquals(VideoZoomMode.STRETCH, VideoZoomMode.FIT.next())
        assertEquals(VideoZoomMode.CROP, VideoZoomMode.STRETCH.next())
        assertEquals(VideoZoomMode.FIT, VideoZoomMode.CROP.next())
    }

    /**
     * 测试函数 `modesMapToMedia3ResizeModes`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `modes Map To Media3 Resize Modes` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun modesMapToMedia3ResizeModes() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, VideoZoomMode.FIT.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, VideoZoomMode.STRETCH.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, VideoZoomMode.CROP.resizeMode)
    }
}
