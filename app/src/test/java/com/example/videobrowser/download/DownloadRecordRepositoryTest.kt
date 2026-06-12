package com.example.videobrowser.download

import com.example.videobrowser.storage.PreferenceStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadRecordRepositoryTest {
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

    @Test
    fun updateStatusReturnsFalseWhenDownloadIdIsUnknown() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())

        assertEquals(false, repository.updateStatus(404L, DownloadStatus.FAILED))
    }

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

    @Test
    fun recordsIgnoreCorruptedStorage() {
        val store = InMemoryPreferenceStore()
        store.putString("download_records", "not-json")

        val repository = DownloadRecordRepository(store)

        assertTrue(repository.records().isEmpty())
    }

    @Test
    fun clearRemovesSavedRecords() {
        val repository = DownloadRecordRepository(InMemoryPreferenceStore())
        repository.add(record(id = 1L, fileName = "first.mp4", createdAtMillis = 10L))

        repository.clear()

        assertTrue(repository.records().isEmpty())
    }

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
