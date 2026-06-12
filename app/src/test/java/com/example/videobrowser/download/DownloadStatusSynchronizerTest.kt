package com.example.videobrowser.download

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadStatusSynchronizerTest {
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
