package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Repository Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.testutil.InMemoryPreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordRepositoryTest {
    /**
     * 测试函数 `recordsArePersistedNewestFirst`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Are Persisted Newest First` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsArePersistedNewestFirst() {
        val store = InMemoryPreferenceStore()
        val repository = DownloadRecordRepository(store)

        repository.add(record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L))
        repository.add(record(id = 2L, fileName = "second.zip", createdAtMillis = 20L))

        val reloaded = DownloadRecordRepository(store)

        assertEquals(
            listOf(
                record(id = 2L, fileName = "second.zip", createdAtMillis = 20L),
                record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L)
            ),
            reloaded.records()
        )
    }

    /**
     * 测试函数 `recordsReplaceExistingDownloadId`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Replace Existing Download Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsReplaceExistingDownloadId() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())

        repository.add(record(id = 4L, fileName = "old.bin", createdAtMillis = 10L))
        repository.add(record(id = 4L, fileName = "new.bin", createdAtMillis = 30L))

        assertEquals(
            listOf(record(id = 4L, fileName = "new.bin", createdAtMillis = 30L)),
            repository.records()
        )
    }

    /**
     * 测试函数 `recordsPersistStatusAndAllowStatusUpdates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Persist Status And Allow Status Updates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsPersistStatusAndAllowStatusUpdates() {
        val store = InMemoryPreferenceStore()
        val repository = DownloadRecordRepository(store)

        repository.add(
            record(
                id = 9L,
                fileName = "movie.mp4",
                createdAtMillis = 90L,
                status = DownloadStatus.IN_PROGRESS
            )
        )

        assertEquals(DownloadStatus.IN_PROGRESS, DownloadRecordRepository(store).records().single().status)
        assertEquals(true, repository.updateStatus(9L, DownloadStatus.COMPLETED))
        assertEquals(DownloadStatus.COMPLETED, DownloadRecordRepository(store).records().single().status)
    }

    /**
     * 测试函数 `recordsPersistStatusReasonAndAllowReasonUpdates`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Persist Status Reason And Allow Reason Updates` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsPersistStatusReasonAndAllowReasonUpdates() {
        val store = InMemoryPreferenceStore()
        val repository = DownloadRecordRepository(store)
        repository.add(
            record(
                id = 12L,
                fileName = "broken.zip",
                createdAtMillis = 120L,
                status = DownloadStatus.IN_PROGRESS
            )
        )

        assertEquals(true, repository.updateStatus(12L, DownloadStatus.FAILED, statusReason = 1006))

        val reloaded = DownloadRecordRepository(store).records().single()
        assertEquals(DownloadStatus.FAILED, reloaded.status)
        assertEquals(1006, reloaded.statusReason)
    }

    /**
     * 测试函数 `recordsPersistProgressSnapshot`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Persist Progress Snapshot` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsPersistProgressSnapshot() {
        val store = InMemoryPreferenceStore()
        val repository = DownloadRecordRepository(store)
        repository.add(
            record(
                id = 13L,
                fileName = "progress.zip",
                createdAtMillis = 130L,
                status = DownloadStatus.IN_PROGRESS
            )
        )

        assertEquals(
            true,
            repository.updateSnapshot(
                downloadId = 13L,
                status = DownloadStatus.IN_PROGRESS,
                bytesDownloaded = 512L,
                totalBytes = 1024L
            )
        )

        val reloaded = DownloadRecordRepository(store).records().single()
        assertEquals(512L, reloaded.bytesDownloaded)
        assertEquals(1024L, reloaded.totalBytes)
        assertEquals(50, reloaded.progress.percent())
    }

    /**
     * 测试函数 `statusUpdatesPreserveExistingProgressSnapshot`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `status Updates Preserve Existing Progress Snapshot` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun statusUpdatesPreserveExistingProgressSnapshot() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(
            record(
                id = 14L,
                fileName = "done.zip",
                createdAtMillis = 140L,
                status = DownloadStatus.IN_PROGRESS
            )
        )
        repository.updateSnapshot(
            downloadId = 14L,
            status = DownloadStatus.IN_PROGRESS,
            bytesDownloaded = 100L,
            totalBytes = 200L
        )

        repository.updateStatus(14L, DownloadStatus.COMPLETED)

        val record = repository.records().single()
        assertEquals(DownloadStatus.COMPLETED, record.status)
        assertEquals(100L, record.bytesDownloaded)
        assertEquals(200L, record.totalBytes)
    }

    /**
     * 测试函数 `legacyRecordsWithoutStatusAreReadAsCompleted`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `legacy Records Without Status Are Read As Completed` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun legacyRecordsWithoutStatusAreReadAsCompleted() {
        val store = InMemoryPreferenceStore()
        store.putString(
            "download_records",
            "10\tlegacy.mp4\thttps://example.com/legacy.mp4\tlegacy.mp4\tvideo/mp4\t100"
        )

        val repository = DownloadRecordRepository(store)

        assertEquals(DownloadStatus.COMPLETED, repository.records().single().status)
        assertEquals(null, repository.records().single().statusReason)
    }

    /**
     * 测试函数 `statusRecordsWithoutReasonRemainCompatible`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `status Records Without Reason Remain Compatible` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun statusRecordsWithoutReasonRemainCompatible() {
        val store = InMemoryPreferenceStore()
        store.putString(
            "download_records",
            "11\told-status.zip\thttps://example.com/old-status.zip\told-status.zip\tapplication/zip\t110\tfailed"
        )

        val repository = DownloadRecordRepository(store)

        assertEquals(DownloadStatus.FAILED, repository.records().single().status)
        assertEquals(null, repository.records().single().statusReason)
    }

    /**
     * 测试函数 `reasonRecordsWithoutProgressRemainCompatible`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `reason Records Without Progress Remain Compatible` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun reasonRecordsWithoutProgressRemainCompatible() {
        val store = InMemoryPreferenceStore()
        store.putString(
            "download_records",
            "15\treason.zip\thttps://example.com/reason.zip\treason.zip\tapplication/zip\t150\tfailed\t1006"
        )

        val repository = DownloadRecordRepository(store)

        val record = repository.records().single()
        assertEquals(DownloadStatus.FAILED, record.status)
        assertEquals(1006, record.statusReason)
        assertEquals(null, record.bytesDownloaded)
        assertEquals(null, record.totalBytes)
    }

    /**
     * 测试函数 `recordsPersistCanceledStatus`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Persist Canceled Status` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsPersistCanceledStatus() {
        val store = InMemoryPreferenceStore()
        val repository = DownloadRecordRepository(store)
        repository.add(
            record(
                id = 16L,
                fileName = "canceled.zip",
                createdAtMillis = 160L,
                status = DownloadStatus.CANCELED
            )
        )

        assertEquals(DownloadStatus.CANCELED, DownloadRecordRepository(store).records().single().status)
    }

    /**
     * 测试函数 `updateStatusReturnsFalseWhenDownloadIdIsUnknown`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `update Status Returns False When Download Id Is Unknown` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun updateStatusReturnsFalseWhenDownloadIdIsUnknown() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())

        assertEquals(false, repository.updateStatus(404L, DownloadStatus.FAILED))
    }

    /**
     * 测试函数 `containsReturnsWhetherDownloadIdExists`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `contains Returns Whether Download Id Exists` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun containsReturnsWhetherDownloadIdExists() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 7L, fileName = "known.zip", createdAtMillis = 70L))

        assertEquals(true, repository.contains(7L))
        assertEquals(false, repository.contains(404L))
    }

    /**
     * 测试函数 `recordsKeepMostRecentEightyEntries`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Keep Most Recent Eighty Entries` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsKeepMostRecentEightyEntries() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())

        (1L..81L).forEach { id ->
            repository.add(record(id = id, fileName = "file-$id.bin", createdAtMillis = id))
        }

        val records = repository.records()

        assertEquals(80, records.size)
        assertEquals(81L, records.first().downloadId)
        assertEquals(2L, records.last().downloadId)
    }

    /**
     * 测试函数 `recordsIgnoreCorruptedStorage`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `records Ignore Corrupted Storage` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun recordsIgnoreCorruptedStorage() {
        val store = InMemoryPreferenceStore()
        store.putString("download_records", "not-json")

        val repository = DownloadRecordRepository(store)

        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `clearRemovesSavedRecords`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `clear Removes Saved Records` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun clearRemovesSavedRecords() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L))

        repository.clear()

        assertTrue(repository.records().isEmpty())
    }

    /**
     * 测试函数 `removeDeletesOneRecordByDownloadId`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Deletes One Record By Download Id` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun removeDeletesOneRecordByDownloadId() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L))
        repository.add(record(id = 2L, fileName = "second.zip", createdAtMillis = 20L))

        assertEquals(true, repository.remove(1L))

        assertEquals(listOf(record(id = 2L, fileName = "second.zip", createdAtMillis = 20L)), repository.records())
    }

    /**
     * 测试函数 `removeReturnsFalseWhenDownloadIdIsUnknown`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove Returns False When Download Id Is Unknown` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    @Test
    fun removeReturnsFalseWhenDownloadIdIsUnknown() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L))

        assertEquals(false, repository.remove(404L))

        assertEquals(1, repository.records().size)
    }

    /**
     * 测试函数 `record`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `record` 这条行为是否成立。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param id 参数类型为 `Long`，表示函数执行 `id` 相关逻辑时需要读取或处理的输入。
     * @param fileName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param createdAtMillis 参数类型为 `Long`，表示函数执行 `createdAtMillis` 相关逻辑时需要读取或处理的输入。
     * @param status 参数类型为 `DownloadStatus`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun record(
        id: Long,
        fileName: String,
        createdAtMillis: Long,
        status: DownloadStatus = DownloadStatus.COMPLETED
    ): DownloadRecord {
        return DownloadRecord(
            downloadId = id,
            title = fileName,
            sourceUrl = "https://example.com/$fileName",
            fileName = fileName,
            mimeType = "application/octet-stream",
            createdAtMillis = createdAtMillis,
            status = status,
            statusReason = null
        )
    }

}
