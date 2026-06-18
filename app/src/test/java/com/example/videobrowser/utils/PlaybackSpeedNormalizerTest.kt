package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackSpeedNormalizerTest {
    @Test
    fun `positive finite speeds are preserved`() {
        assertEquals(0.5f, PlaybackSpeedNormalizer.normalize(0.5f))
        assertEquals(2f, PlaybackSpeedNormalizer.normalize(2f))
    }

    @Test
    fun `invalid speeds fall back to default speed`() {
        assertEquals(1f, PlaybackSpeedNormalizer.normalize(0f))
        assertEquals(1f, PlaybackSpeedNormalizer.normalize(-1f))
        assertEquals(1f, PlaybackSpeedNormalizer.normalize(Float.NaN))
        assertEquals(1f, PlaybackSpeedNormalizer.normalize(Float.POSITIVE_INFINITY))
        assertEquals(1f, PlaybackSpeedNormalizer.normalize(Float.NEGATIVE_INFINITY))
    }

    @Test
    fun `callers can supply a custom fallback speed`() {
        assertEquals(2f, PlaybackSpeedNormalizer.normalize(Float.NaN, defaultSpeed = 2f))
    }
}
