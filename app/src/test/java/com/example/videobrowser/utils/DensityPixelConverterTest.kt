package com.example.videobrowser.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DensityPixelConverterTest {
    @Test
    fun `converts density pixels with explicit rounding policy`() {
        assertEquals(1, DensityPixelConverter.truncateDp(1, 1.5f))
        assertEquals(2, DensityPixelConverter.roundDp(1, 1.5f))
    }
}
