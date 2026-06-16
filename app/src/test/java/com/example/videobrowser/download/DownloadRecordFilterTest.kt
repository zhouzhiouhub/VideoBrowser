package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Filter Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadRecordFilterTest {
    /**
     * 测试函数 `filterReturnsAllRecordsWhenNoFilterIsSelected`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Returns All Records When No Filter Is Selected` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterReturnsAllRecordsWhenNoFilterIsSelected() {
        val records = sampleRecords()

        assertEquals(records, DownloadRecordFilter.filter(records))
    }

    /**
     * 测试函数 `filterMatchesStatus`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches Status` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesStatus() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            status = DownloadStatus.FAILED
        )

        assertEquals(listOf(3L), results.map { record -> record.downloadId })
    }

    /**
     * 测试函数 `filterMatchesCategory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Matches Category` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterMatchesCategory() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            category = DownloadCategory.VIDEO
        )

        assertEquals(listOf(1L, 4L), results.map { record -> record.downloadId })
    }

    /**
     * 测试函数 `filterRequiresStatusAndCategoryWhenBothAreSelected`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `filter Requires Status And Category When Both Are Selected` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun filterRequiresStatusAndCategoryWhenBothAreSelected() {
        val results = DownloadRecordFilter.filter(
            records = sampleRecords(),
            status = DownloadStatus.IN_PROGRESS,
            category = DownloadCategory.VIDEO
        )

        assertEquals(listOf(4L), results.map { record -> record.downloadId })
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
