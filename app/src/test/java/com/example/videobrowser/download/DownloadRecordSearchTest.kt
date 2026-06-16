package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Search Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadRecordSearchTest {
    @Test
    fun filterReturnsAllRecordsForBlankQuery() {
        val records = sampleRecords()

        assertEquals(records, DownloadRecordSearch.filter(records, " "))
    }

    @Test
    fun filterMatchesFileNameAndSourceUrlCaseInsensitively() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "VIDEO EXAMPLE")

        assertEquals(listOf(1L), results.map { record -> record.downloadId })
    }

    @Test
    fun filterRequiresEverySearchTerm() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "manual pdf")

        assertEquals(listOf(2L), results.map { record -> record.downloadId })
    }

    @Test
    fun filterMatchesMimeType() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "application")

        assertEquals(listOf(2L), results.map { record -> record.downloadId })
    }

    private fun sampleRecords(): List<DownloadRecord> {
        return listOf(
            DownloadRecord(
                downloadId = 1L,
                title = "Video Clip",
                sourceUrl = "https://media.example.com/watch",
                fileName = "clip.mp4",
                mimeType = "video/mp4",
                createdAtMillis = 1_700_000_000_000L
            ),
            DownloadRecord(
                downloadId = 2L,
                title = "Manual",
                sourceUrl = "https://docs.example.com/manual",
                fileName = "manual.pdf",
                mimeType = "application/pdf",
                createdAtMillis = 1_700_000_000_001L
            ),
            DownloadRecord(
                downloadId = 3L,
                title = "Image",
                sourceUrl = "https://images.example.com/photo",
                fileName = "photo.jpg",
                mimeType = "image/jpeg",
                createdAtMillis = 1_700_000_000_002L
            )
        )
    }
}
