package com.example.videobrowser.video

/**
 * 测试阅读提示：
 * 这个测试文件验证“Video Seek Drag Calculator Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoSeekDragCalculatorTest {
    /**
     * 测试函数 `unknown`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `unknown` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun `unknown duration uses a one minute seek span`() {
        assertEquals(60_000L, VideoSeekDragCalculator.seekSpanForDuration(null))
        assertEquals(60_000L, VideoSeekDragCalculator.seekSpanForDuration(0L))
    }

    /**
     * 测试函数 `known`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `known` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun `known short duration uses the media duration as seek span`() {
        assertEquals(30_000L, VideoSeekDragCalculator.seekSpanForDuration(30_000L))
    }

    /**
     * 测试函数 `known`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `known` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun `known long duration caps the seek span to ten minutes`() {
        val twoHoursMs = 2L * 60L * 60L * 1000L

        assertEquals(600_000L, VideoSeekDragCalculator.seekSpanForDuration(twoHoursMs))
    }

    /**
     * 测试函数 `drag`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `drag` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun `drag target is based on the capped seek span and clamps to duration`() {
        val twoHoursMs = 2L * 60L * 60L * 1000L

        val halfWidthDragTarget = VideoSeekDragCalculator.targetForDrag(
            startPositionMs = 3_600_000L,
            durationMs = twoHoursMs,
            deltaX = 500f,
            viewWidth = 1_000
        )
        val overEndTarget = VideoSeekDragCalculator.targetForDrag(
            startPositionMs = twoHoursMs - 1_000L,
            durationMs = twoHoursMs,
            deltaX = 1_000f,
            viewWidth = 1_000
        )
        val beforeStartTarget = VideoSeekDragCalculator.targetForDrag(
            startPositionMs = 1_000L,
            durationMs = twoHoursMs,
            deltaX = -1_000f,
            viewWidth = 1_000
        )

        assertEquals(3_900_000L, halfWidthDragTarget)
        assertEquals(twoHoursMs, overEndTarget)
        assertEquals(0L, beforeStartTarget)
    }

    /**
     * 测试函数 `drag`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `drag` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun `drag target ignores invalid width`() {
        assertEquals(
            42_000L,
            VideoSeekDragCalculator.targetForDrag(
                startPositionMs = 42_000L,
                durationMs = 120_000L,
                deltaX = 500f,
                viewWidth = 0
            )
        )
    }
}
