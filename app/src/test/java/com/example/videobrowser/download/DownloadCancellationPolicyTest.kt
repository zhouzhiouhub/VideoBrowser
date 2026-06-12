package com.example.videobrowser.download

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadCancellationPolicyTest {
    @Test
    fun onlyInProgressDownloadsCanBeCanceled() {
        assertTrue(DownloadCancellationPolicy.canCancel(record(DownloadStatus.IN_PROGRESS)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.COMPLETED)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.FAILED)))
        assertFalse(DownloadCancellationPolicy.canCancel(record(DownloadStatus.CANCELED)))
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
