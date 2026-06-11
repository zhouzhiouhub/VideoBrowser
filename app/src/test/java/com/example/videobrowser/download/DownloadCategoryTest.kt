package com.example.videobrowser.download

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadCategoryTest {
    @Test
    fun classifiesByMimeTypeBeforeFileExtension() {
        assertEquals(
            DownloadCategory.IMAGE,
            DownloadCategory.from(mimeType = "image/jpeg", fileName = "download.bin")
        )
        assertEquals(
            DownloadCategory.VIDEO,
            DownloadCategory.from(mimeType = "video/mp4; charset=utf-8", fileName = "clip.dat")
        )
        assertEquals(
            DownloadCategory.APP,
            DownloadCategory.from(
                mimeType = "application/vnd.android.package-archive",
                fileName = "package.zip"
            )
        )
    }

    @Test
    fun classifiesByCommonFileExtensionsWhenMimeTypeIsMissingOrGeneric() {
        assertEquals(DownloadCategory.IMAGE, DownloadCategory.from(null, "poster.webp"))
        assertEquals(DownloadCategory.VIDEO, DownloadCategory.from("application/octet-stream", "movie.mkv"))
        assertEquals(DownloadCategory.AUDIO, DownloadCategory.from(null, "song.flac"))
        assertEquals(DownloadCategory.DOCUMENT, DownloadCategory.from(null, "paper.pdf"))
        assertEquals(DownloadCategory.APP, DownloadCategory.from(null, "release.apk"))
        assertEquals(DownloadCategory.ARCHIVE, DownloadCategory.from(null, "bundle.7z"))
    }

    @Test
    fun groupsRecordsInStableCategoryOrderAndKeepsRecordOrderInsideEachCategory() {
        val records = listOf(
            record(id = 1L, fileName = "latest.zip"),
            record(id = 2L, fileName = "poster.png"),
            record(id = 3L, fileName = "clip.mp4"),
            record(id = 4L, fileName = "manual.pdf"),
            record(id = 5L, fileName = "screenshot.jpg")
        )

        val groups = DownloadCategoryGroup.from(records)

        assertEquals(
            listOf(
                DownloadCategory.VIDEO,
                DownloadCategory.IMAGE,
                DownloadCategory.DOCUMENT,
                DownloadCategory.ARCHIVE
            ),
            groups.map { it.category }
        )
        assertEquals(listOf(3L), groups[0].records.map { it.downloadId })
        assertEquals(listOf(2L, 5L), groups[1].records.map { it.downloadId })
    }

    private fun record(id: Long, fileName: String): DownloadRecord {
        return DownloadRecord(
            downloadId = id,
            title = fileName,
            sourceUrl = "https://example.com/$fileName",
            fileName = fileName,
            mimeType = null,
            createdAtMillis = id
        )
    }
}
