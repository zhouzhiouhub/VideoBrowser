package com.example.videobrowser.download

/**
 * 测试阅读提示：
 * 这个测试文件验证“Download Record Remover Test”相关行为。
 * 初学者可以先看每个 @Test 函数名了解被验证的功能，再看断言确认代码需要满足哪些条件。
 */
import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordRemoverTest {
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

    private class InMemoryPreferenceStore : PreferenceStore {
        private val values = mutableMapOf<String, Any>()

        override fun contains(key: String): Boolean = values.containsKey(key)

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
            values[key] as? Boolean ?: defaultValue

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float =
            values[key] as? Float ?: defaultValue

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? =
            values[key] as? String ?: defaultValue

        override fun putString(key: String, value: String) {
            values[key] = value
        }

        override fun remove(key: String) {
            values.remove(key)
        }

        override fun remove(keys: Iterable<String>, commit: Boolean): Boolean {
            keys.forEach { key -> values.remove(key) }
            return true
        }
    }
}
