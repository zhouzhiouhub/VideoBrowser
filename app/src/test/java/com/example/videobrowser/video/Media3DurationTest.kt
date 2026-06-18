package com.example.videobrowser.video

import androidx.media3.common.C
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Media3DurationTest {
    @Test
    fun knownDurationRejectsUnsetZeroAndNegativeValues() {
        assertNull(Media3Duration.knownDurationMs(C.TIME_UNSET))
        assertNull(Media3Duration.knownDurationMs(0L))
        assertNull(Media3Duration.knownDurationMs(-10L))
    }

    @Test
    fun knownDurationKeepsPositiveValues() {
        assertEquals(12_000L, Media3Duration.knownDurationMs(12_000L))
    }

    @Test
    fun durationOrZeroReturnsZeroForUnknownDurations() {
        assertEquals(0L, Media3Duration.durationOrZero(C.TIME_UNSET))
        assertEquals(8_000L, Media3Duration.durationOrZero(8_000L))
    }

    @Test
    fun boundedSeekPositionClampsWhenDurationIsKnown() {
        assertEquals(0L, Media3Duration.boundedSeekPositionMs(-500L, 10_000L))
        assertEquals(4_000L, Media3Duration.boundedSeekPositionMs(4_000L, 10_000L))
        assertEquals(10_000L, Media3Duration.boundedSeekPositionMs(12_000L, 10_000L))
    }

    @Test
    fun boundedSeekPositionOnlyClampsToZeroWhenDurationIsUnknown() {
        assertEquals(0L, Media3Duration.boundedSeekPositionMs(-500L, C.TIME_UNSET))
        assertEquals(12_000L, Media3Duration.boundedSeekPositionMs(12_000L, C.TIME_UNSET))
    }
}
