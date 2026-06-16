package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Category Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadCategoryTest {
    /**
     * 测试函数 `classifiesByMimeTypeBeforeFileExtension`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `classifies By Mime Type Before File Extension` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `classifiesByCommonFileExtensionsWhenMimeTypeIsMissingOrGeneric`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `classifies By Common File Extensions When Mime Type Is Missing Or Generic` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun classifiesByCommonFileExtensionsWhenMimeTypeIsMissingOrGeneric() {
        assertEquals(DownloadCategory.IMAGE, DownloadCategory.from(null, "poster.webp"))
        assertEquals(DownloadCategory.VIDEO, DownloadCategory.from("application/octet-stream", "movie.mkv"))
        assertEquals(DownloadCategory.AUDIO, DownloadCategory.from(null, "song.flac"))
        assertEquals(DownloadCategory.DOCUMENT, DownloadCategory.from(null, "paper.pdf"))
        assertEquals(DownloadCategory.APP, DownloadCategory.from(null, "release.apk"))
        assertEquals(DownloadCategory.ARCHIVE, DownloadCategory.from(null, "bundle.7z"))
    }

    /**
     * 测试函数 `groupsRecordsInStableCategoryOrderAndKeepsRecordOrderInsideEachCategory`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `groups Records In Stable Category Order And Keeps Record Order Inside Each Category` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
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

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `Long`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
