package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Status Synchronizer Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadStatusSynchronizerTest {
    /**
     * 测试函数 `refreshUpdatesOnlyInProgressRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `refresh Updates Only In Progress Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun refreshUpdatesOnlyInProgressRecords() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, status = DownloadStatus.IN_PROGRESS))
        repository.add(record(id = 2L, status = DownloadStatus.COMPLETED))
        val queriedIds = mutableListOf<Long>()

        val refreshed = DownloadStatusSynchronizer(repository) { id ->
            queriedIds += id
            DownloadStatusSnapshot(
                status = DownloadStatus.IN_PROGRESS,
                bytesDownloaded = 50L,
                totalBytes = 100L
            )
        }.refresh()

        assertEquals(listOf(1L), queriedIds)
        assertEquals(50L, refreshed.first { it.downloadId == 1L }.bytesDownloaded)
        assertEquals(null, refreshed.first { it.downloadId == 2L }.bytesDownloaded)
    }

    /**
     * 测试函数 `refreshPersistsCompletedSnapshot`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `refresh Persists Completed Snapshot` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun refreshPersistsCompletedSnapshot() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 3L, status = DownloadStatus.IN_PROGRESS))

        DownloadStatusSynchronizer(repository) {
            DownloadStatusSnapshot(
                status = DownloadStatus.COMPLETED,
                bytesDownloaded = 100L,
                totalBytes = 100L
            )
        }.refresh()

        val record = repository.records().single()
        assertEquals(DownloadStatus.COMPLETED, record.status)
        assertEquals(100L, record.bytesDownloaded)
        assertEquals(100L, record.totalBytes)
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
            title = "file-$id.bin",
            sourceUrl = "https://example.com/file-$id.bin",
            fileName = "file-$id.bin",
            mimeType = "application/octet-stream",
            createdAtMillis = id,
            status = status
        )
    }

}
