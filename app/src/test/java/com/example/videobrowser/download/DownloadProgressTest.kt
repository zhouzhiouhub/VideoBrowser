package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Progress Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
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
