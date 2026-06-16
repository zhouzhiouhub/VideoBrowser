package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Cleaner Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.PreferenceStore
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

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        /**
         * 测试函数 `contains`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `contains` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        /**
         * 测试函数 `getBoolean`：按测试名描述的场景准备输入、调用被测代码，并用断言验证 `get Boolean` 这条行为是否成立。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param key 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
         * @param defaultValue 参数类型为 `Boolean`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

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
        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

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
        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

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
