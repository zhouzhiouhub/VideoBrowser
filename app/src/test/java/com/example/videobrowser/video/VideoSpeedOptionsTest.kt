package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Speed Options Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoSpeedOptionsTest {
    @Test
    fun `menu speeds are sorted and include conservative iris inspired stops`() {
        val speeds = VideoSpeedOptions.menuSpeeds()

        assertEquals(listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 2.5f, 3f), speeds)
        assertTrue(speeds.zipWithNext().all { (previous, next) -> previous < next })
    }

    @Test
    fun `speed menu keeps browser safe playback bounds`() {
        val speeds = VideoSpeedOptions.menuSpeeds()

        assertEquals(0.5f, speeds.first())
        assertEquals(3f, speeds.last())
        assertTrue(1f in speeds)
    }

    @Test
    fun `long press speed stays at a predictable temporary boost`() {
        assertEquals(2f, VideoSpeedOptions.longPressSpeed)
    }
}
