package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Canceller Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadCancellerTest {
    /**
     * 测试函数 `cancelRemovesSystemDownloadAndMarksRecordCanceled`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `cancel Removes System Download And Marks Record Canceled` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun cancelRemovesSystemDownloadAndMarksRecordCanceled() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 8L, status = DownloadStatus.IN_PROGRESS))
        val removedIds = mutableListOf<List<Long>>()
        val canceller = DownloadCanceller(repository) { ids ->
            removedIds += ids.toList()
            ids.size
        }

        val result = canceller.cancel(repository.records().single())

        assertEquals(listOf(listOf(8L)), removedIds)
        assertEquals(1, result.requestedSystemDownloadCount)
        assertEquals(1, result.removedSystemDownloadCount)
        assertEquals(true, result.statusUpdated)
        assertEquals(true, result.canceled)
        assertEquals(DownloadStatus.CANCELED, repository.records().single().status)
    }

    /**
     * 测试函数 `cancelDoesNotUpdateRecordWhenSystemDownloadIsNotRemoved`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `cancel Does Not Update Record When System Download Is Not Removed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun cancelDoesNotUpdateRecordWhenSystemDownloadIsNotRemoved() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 9L, status = DownloadStatus.IN_PROGRESS))
        val canceller = DownloadCanceller(repository) { 0 }

        val result = canceller.cancel(repository.records().single())

        assertEquals(1, result.requestedSystemDownloadCount)
        assertEquals(0, result.removedSystemDownloadCount)
        assertEquals(false, result.statusUpdated)
        assertEquals(false, result.canceled)
        assertEquals(DownloadStatus.IN_PROGRESS, repository.records().single().status)
    }

    /**
     * 测试函数 `cancelIgnoresNonInProgressRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `cancel Ignores Non In Progress Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun cancelIgnoresNonInProgressRecords() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 10L, status = DownloadStatus.COMPLETED))
        var removerCalled = false
        val canceller = DownloadCanceller(repository) {
            removerCalled = true
            1
        }

        val result = canceller.cancel(repository.records().single())

        assertEquals(0, result.requestedSystemDownloadCount)
        assertEquals(0, result.removedSystemDownloadCount)
        assertEquals(false, result.statusUpdated)
        assertEquals(false, result.canceled)
        assertEquals(false, removerCalled)
        assertEquals(DownloadStatus.COMPLETED, repository.records().single().status)
    }

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `Long`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param status 参数类型为 `DownloadStatus`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun record(id: Long, status: DownloadStatus): DownloadRecord {
        return DownloadRecord(
            downloadId = id,
            title = "file-$id.zip",
            sourceUrl = "https://example.com/file-$id.zip",
            fileName = "file-$id.zip",
            mimeType = "application/zip",
            createdAtMillis = id,
            status = status
        )
    }

}
