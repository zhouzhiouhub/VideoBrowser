package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationLabelFormatterTest {
    @Test
    fun `formats minutes without leading zero by default`() {
        assertEquals("1:05", DurationLabelFormatter.formatMillis(65_000L))
        assertEquals("0:04", DurationLabelFormatter.formatMillis(4_000L))
    }

    @Test
    fun `formats minutes with leading zero when requested`() {
        assertEquals(
            "01:05",
            DurationLabelFormatter.formatMillis(
                65_000L,
                minuteStyle = DurationLabelFormatter.MinuteStyle.TWO_DIGIT
            )
        )
    }

    @Test
    fun `formats hour durations consistently`() {
        assertEquals("1:01:05", DurationLabelFormatter.formatMillis(3_665_000L))
    }

    @Test
    fun `clamps negative durations to zero`() {
        assertEquals("0:00", DurationLabelFormatter.formatMillis(-1L))
    }
}
