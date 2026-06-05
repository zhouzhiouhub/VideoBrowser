package com.example.videobrowser.download

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordCleanerTest {
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

        override fun contains(key: String): Boolean {
            return values.containsKey(key)
        }

        override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
            return values[key] as? Boolean ?: defaultValue
        }

        override fun putBoolean(key: String, value: Boolean) {
            values[key] = value
        }

        override fun getFloat(key: String, defaultValue: Float): Float {
            return values[key] as? Float ?: defaultValue
        }

        override fun putFloat(key: String, value: Float) {
            values[key] = value
        }

        override fun getString(key: String, defaultValue: String?): String? {
            return values[key] as? String ?: defaultValue
        }

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
