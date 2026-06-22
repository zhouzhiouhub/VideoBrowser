package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Cleaner Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordCleanerTest {
    /**
     * 测试函数 `clearRecordsAndFilesRemovesTrackedSystemDownloadsAndClearsRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Records And Files Removes Tracked System Downloads And Clears Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRecordsAndFilesRemovesTrackedSystemDownloadsAndClearsRecords() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, fileName = "first.mp4"))
        repository.add(record(id = 2L, fileName = "second.zip"))
        val removedIds = mutableListOf<List<Long>>()
        val cleaner = DownloadRecordCleaner(repository) { ids ->
            removedIds += ids.toList()
            ids.size
        }

        val result = cleaner.clearRecordsAndFiles()

        assertEquals(listOf(listOf(2L, 1L)), removedIds)
        assertEquals(2, result.requestedSystemDownloadCount)
        assertEquals(2, result.removedSystemDownloadCount)
        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `clearRecordsAndFilesDoesNotCallSystemRemoverWhenRecordsAreEmpty`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Records And Files Does Not Call System Remover When Records Are Empty` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRecordsAndFilesDoesNotCallSystemRemoverWhenRecordsAreEmpty() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        var removerCalled = false
        val cleaner = DownloadRecordCleaner(repository) {
            removerCalled = true
            0
        }

        val result = cleaner.clearRecordsAndFiles()

        assertEquals(0, result.requestedSystemDownloadCount)
        assertEquals(0, result.removedSystemDownloadCount)
        assertTrue(repository.records().isEmpty())
        assertEquals(false, removerCalled)
    }

    /**
     * 测试函数 `clearRecordsAndFilesStillClearsRecordsWhenSystemRemovalFails`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Records And Files Still Clears Records When System Removal Fails` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRecordsAndFilesStillClearsRecordsWhenSystemRemovalFails() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 7L, fileName = "partial.bin"))
        val cleaner = DownloadRecordCleaner(repository) {
            throw IllegalStateException("download manager unavailable")
        }

        val result = cleaner.clearRecordsAndFiles()

        assertEquals(1, result.requestedSystemDownloadCount)
        assertEquals(0, result.removedSystemDownloadCount)
        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `Long`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun record(
        id: Long,
        fileName: String
    ): DownloadRecord {
        return DownloadRecord(
            downloadId = id,
            title = fileName,
            sourceUrl = "https://example.com/$fileName",
            fileName = fileName,
            mimeType = "application/octet-stream",
            createdAtMillis = id
        )
    }

}
