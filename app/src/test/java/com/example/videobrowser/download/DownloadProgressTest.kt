package com.example.videobrowser.download

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DownloadProgressTest {
    @Test
    fun percentUsesKnownTotal() {
        assertEquals(42, DownloadProgress(bytesDownloaded = 42L, totalBytes = 100L).percent())
    }

    @Test
    fun percentCapsAtOneHundred() {
        assertEquals(100, DownloadProgress(bytesDownloaded = 120L, totalBytes = 100L).percent())
    }

    @Test
    fun percentIsUnknownWithoutTotal() {
        assertNull(DownloadProgress(bytesDownloaded = 42L, totalBytes = -1L).percent())
        assertNull(DownloadProgress(bytesDownloaded = 42L, totalBytes = null).percent())
    }
}
