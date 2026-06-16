package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Search Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadRecordSearchTest {
    /**
     * 测试函数 `filterReturnsAllRecordsForBlankQuery`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Returns All Records For Blank Query` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterReturnsAllRecordsForBlankQuery() {
        val records = sampleRecords()

        assertEquals(records, DownloadRecordSearch.filter(records, " "))
    }

    /**
     * 测试函数 `filterMatchesFileNameAndSourceUrlCaseInsensitively`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches File Name And Source Url Case Insensitively` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesFileNameAndSourceUrlCaseInsensitively() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "VIDEO EXAMPLE")

        assertEquals(listOf(1L), results.map { record -> record.downloadId })
    }

    /**
     * 测试函数 `filterRequiresEverySearchTerm`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Requires Every Search Term` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterRequiresEverySearchTerm() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "manual pdf")

        assertEquals(listOf(2L), results.map { record -> record.downloadId })
    }

    /**
     * 测试函数 `filterMatchesMimeType`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches Mime Type` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesMimeType() {
        val results = DownloadRecordSearch.filter(sampleRecords(), "application")

        assertEquals(listOf(2L), results.map { record -> record.downloadId })
    }

    /**
     * 测试函数 `sampleRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `sample Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
