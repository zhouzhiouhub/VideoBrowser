package com.example.videobrowser.video

import org.junit.Assert.assertEquals
import org.junit.Test

class VideoGestureFeedbackFormatterTest {
    @Test
    fun `brightness feedback formats a clamped percent`() {
        assertEquals("\u2600 38%", VideoGestureFeedbackFormatter.formatBrightness(0.375f))
        assertEquals("\u2600 0%", VideoGestureFeedbackFormatter.formatBrightness(-1f))
        assertEquals("\u2600 100%", VideoGestureFeedbackFormatter.formatBrightness(2f))
    }

    @Test
    fun `volume feedback formats stream percent`() {
        assertEquals("\ud83d\udd0a 50%", VideoGestureFeedbackFormatter.formatVolume(5, 0, 10))
        assertEquals("\ud83d\udd0a 0%", VideoGestureFeedbackFormatter.formatVolume(0, 10, 10))
    }

    @Test
    fun `seek and speed feedback keep existing labels`() {
        assertEquals("1x", VideoGestureFeedbackFormatter.formatSpeed(1f))
        assertEquals("1.25x", VideoGestureFeedbackFormatter.formatSpeed(1.25f))
        assertEquals("+10s", VideoGestureFeedbackFormatter.formatSeekSeconds(10))
        assertEquals("-10s", VideoGestureFeedbackFormatter.formatSeekSeconds(-10))
        assertEquals(
            "0s\n01:05 / 01:10",
            VideoGestureFeedbackFormatter.formatSeekPreview(0L, 65_000L, 70_000L)
        )
    }
}
