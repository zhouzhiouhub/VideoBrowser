package com.example.videobrowser.download

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadRecordFilterTest {
    @Test
    fun filterReturnsAllRecordsWhenNoFilterIsSelected() {
        val records = sampleRecords()

        assertEquals(records, DownloadRecordFilter.filter(records))
    }

    @Test
    fun filterMatchesStatus() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            status = DownloadStatus.FAILED
        )

        assertEquals(listOf(3L), results.map { record -> record.downloadId })
    }

    @Test
    fun filterMatchesCategory() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            category = DownloadCategory.VIDEO
        )

        assertEquals(listOf(1L, 4L), results.map { record -> record.downloadId })
    }

    @Test
    fun filterRequiresStatusAndCategoryWhenBothAreSelected() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            status = DownloadStatus.IN_PROGRESS,
            category = DownloadCategory.VIDEO
        )

        assertEquals(listOf(4L), results.map { record -> record.downloadId })
    }

    private fun sampleRecords(): List<DownloadRecord> {
        return listOf(
            DownloadRecord(
                downloadId = 1L,
                title = "Completed Video",
                sourceUrl = "https://media.example.com/video",
                fileName = "video.mp4",
                mimeType = "video/mp4",
                createdAtMillis = 1_700_000_000_000L,
                status = DownloadStatus.COMPLETED
            ),
            DownloadRecord(
                downloadId = 2L,
                title = "Document",
                sourceUrl = "https://docs.example.com/manual",
                fileName = "manual.pdf",
                mimeType = "application/pdf",
                createdAtMillis = 1_700_000_000_001L,
                status = DownloadStatus.COMPLETED
            ),
            DownloadRecord(
                downloadId = 3L,
                title = "Failed Image",
                sourceUrl = "https://images.example.com/photo",
                fileName = "photo.jpg",
                mimeType = "image/jpeg",
                createdAtMillis = 1_700_000_000_002L,
                status = DownloadStatus.FAILED
            ),
            DownloadRecord(
                downloadId = 4L,
                title = "Loading Video",
                sourceUrl = "https://media.example.com/live",
                fileName = "live.m3u8",
                mimeType = null,
                createdAtMillis = 1_700_000_000_003L,
                status = DownloadStatus.IN_PROGRESS
            )
        )
    }
}
