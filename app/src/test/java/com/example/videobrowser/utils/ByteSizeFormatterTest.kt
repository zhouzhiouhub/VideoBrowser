package com.example.videobrowser.utils

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ByteSizeFormatterTest {
    @Test
    fun `formats whole byte values without decimals by default`() {
        assertEquals("42 B", ByteSizeFormatter.format(42L))
    }

    @Test
    fun `formats larger values with one decimal`() {
        assertEquals("1.5 KB", ByteSizeFormatter.format(1536L))
    }

    @Test
    fun `can preserve decimal byte labels for existing browser data summaries`() {
        assertEquals(
            "1.0 B",
            ByteSizeFormatter.format(
                1L,
                maxUnit = ByteSizeFormatter.MaxUnit.GB,
                locale = Locale.US,
                decimalBytes = true
            )
        )
    }

    @Test
    fun `clamps negative sizes to zero`() {
        assertEquals("0 B", ByteSizeFormatter.format(-1L))
    }

    @Test
    fun `supports terabyte labels for local files`() {
        assertEquals("1.0 TB", ByteSizeFormatter.format(1024L * 1024L * 1024L * 1024L))
    }
}
