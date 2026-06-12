package com.example.videobrowser.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRetryPolicyTest {
    @Test
    fun onlyFailedDownloadsCanBeRetried() {
        assertTrue(DownloadRetryPolicy.canRetry(record(DownloadStatus.FAILED)))
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
