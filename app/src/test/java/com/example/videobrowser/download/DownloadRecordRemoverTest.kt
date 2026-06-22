package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Remover Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordRemoverTest {
    /**
     * 测试函数 `removeDeletesSystemDownloadAndRecord`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Deletes System Download And Record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun removeDeletesSystemDownloadAndRecord() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 4L))
        val removedIds = mutableListOf<List<Long>>()
        val remover = DownloadRecordRemover(repository) { ids ->
            removedIds += ids.toList()
            ids.size
        }

        val result = remover.remove(repository.records().single())

        assertEquals(listOf(listOf(4L)), removedIds)
        assertEquals(1, result.requestedSystemDownloadCount)
        assertEquals(1, result.removedSystemDownloadCount)
        assertEquals(true, result.recordRemoved)
        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `removeStillDeletesRecordWhenSystemRemovalFails`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Still Deletes Record When System Removal Fails` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun removeStillDeletesRecordWhenSystemRemovalFails() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 5L))
        val remover = DownloadRecordRemover(repository) {
            throw IllegalStateException("download manager unavailable")
        }

        val result = remover.remove(repository.records().single())

        assertEquals(1, result.requestedSystemDownloadCount)
        assertEquals(0, result.removedSystemDownloadCount)
        assertEquals(true, result.recordRemoved)
        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `Long`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun record(id: Long): DownloadRecord {
        return DownloadRecord(
            downloadId = id,
            title = "file-$id.zip",
            sourceUrl = "https://example.com/file-$id.zip",
            fileName = "file-$id.zip",
            mimeType = "application/zip",
            createdAtMillis = id
        )
    }

}
