package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoSeekDragCalculatorTest {
    @Test
    fun `unknown duration uses a one minute seek span`() {
        assertEquals(60_000L, VideoSeekDragCalculator.seekSpanForDuration(null))
        assertEquals(60_000L, VideoSeekDragCalculator.seekSpanForDuration(0L))
    }

    @Test
    fun `known short duration uses the media duration as seek span`() {
        assertEquals(30_000L, VideoSeekDragCalculator.seekSpanForDuration(30_000L))
    }

    @Test
    fun `known long duration caps the seek span to ten minutes`() {
        val twoHoursMs = 2L * 60L * 60L * 1000L

        assertEquals(600_000L, VideoSeekDragCalculator.seekSpanForDuration(twoHoursMs))
    }

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
