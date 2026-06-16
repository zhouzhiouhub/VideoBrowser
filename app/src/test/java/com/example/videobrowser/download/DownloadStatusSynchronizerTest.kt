package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Status Synchronizer Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.PreferenceStore
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

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        /**
         * 测试函数 `contains`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `contains` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun contains(key: String): Boolean = values.containsKey(key)

        /**
         * 测试函数 `getBoolean`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Boolean` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key] as? Boolean ?: defaultValue

        /**
         * 测试函数 `putBoolean`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put Boolean` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        /**
         * 测试函数 `getFloat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Float` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getFloat(key: String, defaultValue: Float): Float =
            values[key] as? Float ?: defaultValue

        /**
         * 测试函数 `putFloat`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put Float` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `Float`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        /**
         * 测试函数 `getString`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get String` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `String?`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getString(key: String, defaultValue: String?): String? =
            values[key] as? String ?: defaultValue

        /**
         * 测试函数 `putString`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `put String` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         */
        override fun putString(key: String, value: String) {
            values[key] = value
        }

        /**
         * 测试函数 `remove`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         */
        override fun remove(key: String) {
            values.remove(key)
        }

        /**
         * 测试函数 `remove`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `remove` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param keys 参数类型为 `Iterable<String>`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param commit 参数类型为 `Boolean`，表示函数执行 `commit` 相关逻辑时需要读取或处理的输入。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }
}
