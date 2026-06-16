package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Retry Policy Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRetryPolicyTest {
    @Test
    fun failedAndCanceledDownloadsCanBeRetried() {
        assertTrue(DownloadRetryPolicy.canRetry(record(DownloadStatus.FAILED)))
        assertTrue(DownloadRetryPolicy.canRetry(record(DownloadStatus.CANCELED)))
        assertFalse(DownloadRetryPolicy.canRetry(record(DownloadStatus.IN_PROGRESS)))
        assertFalse(DownloadRetryPolicy.canRetry(record(DownloadStatus.COMPLETED)))
    }

    private fun record(status: DownloadStatus): DownloadRecord {
        return DownloadRecord(
            downloadId = 1L,
            title = "file.zip",
            sourceUrl = "https://example.com/file.zip",
            fileName = "file.zip",
            mimeType = "application/zip",
            createdAtMillis = 1L,
            status = status
        )
    }
}
