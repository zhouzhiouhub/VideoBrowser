package com.example.videobrowser.version

import org.junit.Assert.assertEquals
import org.junit.Test

class AppVersionFormatterTest {
    @Test
    fun formatsCommitCountAsFourPartDecimalVersion() {
        assertEquals("0.0.0.0", AppVersionFormatter.formatCommitCount(0))
        assertEquals("0.0.0.1", AppVersionFormatter.formatCommitCount(1))
        assertEquals("0.0.0.9", AppVersionFormatter.formatCommitCount(9))
        assertEquals("0.0.1.0", AppVersionFormatter.formatCommitCount(10))
        assertEquals("0.1.0.0", AppVersionFormatter.formatCommitCount(100))
        assertEquals("1.0.0.0", AppVersionFormatter.formatCommitCount(1000))
        assertEquals("1.2.3.4", AppVersionFormatter.formatCommitCount(1234))
    }

    @Test
    fun negativeCommitCountsFormatAsZeroVersion() {
        assertEquals("0.0.0.0", AppVersionFormatter.formatCommitCount(-1))
    }
}
