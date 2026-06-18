package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedDisplayFormatterTest {
    @Test
    fun `format removes redundant decimals and normalizes invalid speeds`() {
        assertEquals("1x", PlaybackSpeedDisplayFormatter.format(1f))
        assertEquals("1.25x", PlaybackSpeedDisplayFormatter.format(1.25f))
        assertEquals("1.5x", PlaybackSpeedDisplayFormatter.format(1.5f))
        assertEquals("1x", PlaybackSpeedDisplayFormatter.format(Float.NaN))
    }
}
