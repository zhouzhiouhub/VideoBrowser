package com.example.videobrowser.download

import com.example.videobrowser.storage.PreferenceStore

class DownloadRecordRepository(
    private val preferenceStore: PreferenceStore
) {
    fun add(record: DownloadRecord) {
        val records = records()
            .filterNot { item -> item.downloadId == record.downloadId }
            .toMutableList()
        records.add(0, record)
        save(records.take(RECORD_LIMIT))
    }

    fun updateStatus(
        downloadId: Long,
        status: DownloadStatus,
        statusReason: Int? = null
    ): Boolean {
        return updateSnapshot(
            downloadId = downloadId,
            status = status,
            statusReason = statusReason,
            bytesDownloaded = null,
            totalBytes = null
        )
    }

    fun updateSnapshot(
        downloadId: Long,
        status: DownloadStatus,
        statusReason: Int? = null,
        bytesDownloaded: Long? = null,
        totalBytes: Long? = null
    ): Boolean {
        var updated = false
        val updatedRecords = records().map { record ->
            if (record.downloadId == downloadId) {
                updated = true
                record.copy(
                    status = status,
                    statusReason = statusReason,
                    bytesDownloaded = bytesDownloaded?.takeIf { it >= 0L } ?: record.bytesDownloaded,
                    totalBytes = totalBytes?.takeIf { it >= 0L } ?: record.totalBytes
                )
            } else {
                record
            }
        }
        if (!updated) {
            return false
        }
        save(updatedRecords)
        return true
    }

    fun records(): List<DownloadRecord> {
        val rawValue = preferenceStore.getString(KEY_DOWNLOAD_RECORDS, null) ?: return emptyList()
        return rawValue
            .lineSequence()
            .mapNotNull(::parseRecord)
            .toList()
    }

    fun contains(downloadId: Long): Boolean {
        return records().any { record -> record.downloadId == downloadId }
    }

    fun remove(downloadId: Long): Boolean {
        val existingRecords = records()
        val updatedRecords = existingRecords.filterNot { record -> record.downloadId == downloadId }
        if (updatedRecords.size == existingRecords.size) {
            return false
        }
        save(updatedRecords)
        return true
    }

    fun clear() {
        preferenceStore.remove(KEY_DOWNLOAD_RECORDS)
    }

    private fun save(records: List<DownloadRecord>) {
        if (records.isEmpty()) {
            preferenceStore.remove(KEY_DOWNLOAD_RECORDS)
            return
        }

        preferenceStore.putString(
            KEY_DOWNLOAD_RECORDS,
            records.joinToString(separator = "\n", transform = ::encodeRecord)
        )
    }

    private fun parseRecord(line: String): DownloadRecord? {
        val fields = splitEscaped(line)
        if (fields.size != LEGACY_FIELD_COUNT &&
            fields.size != STATUS_FIELD_COUNT &&
            fields.size != STATUS_REASON_FIELD_COUNT &&
            fields.size != FIELD_COUNT
        ) {
            return null
        }
        val downloadId = fields[0].toLongOrNull() ?: return null
        val sourceUrl = fields[2].takeIf { it.isNotBlank() } ?: return null
        val fileName = fields[3].takeIf { it.isNotBlank() } ?: return null
        val createdAtMillis = fields[5].toLongOrNull() ?: return null
        val status = if (fields.size >= STATUS_FIELD_COUNT) {
            DownloadStatus.fromStorage(fields[6]) ?: return null
        } else {
            DownloadStatus.COMPLETED
        }
        val statusReason = if (fields.size >= STATUS_REASON_FIELD_COUNT) {
            fields[7].takeIf { it.isNotBlank() }?.toIntOrNull()
        } else {
            null
        }
        val bytesDownloaded = if (fields.size == FIELD_COUNT) {
            fields[8].takeIf { it.isNotBlank() }?.toLongOrNull()?.takeIf { it >= 0L }
        } else {
            null
        }
        val totalBytes = if (fields.size == FIELD_COUNT) {
            fields[9].takeIf { it.isNotBlank() }?.toLongOrNull()?.takeIf { it >= 0L }
        } else {
            null
        }
        return DownloadRecord(
            downloadId = downloadId,
            title = fields[1],
            sourceUrl = sourceUrl,
            fileName = fileName,
            mimeType = fields[4].takeIf { it.isNotBlank() },
            createdAtMillis = createdAtMillis,
            status = status,
            statusReason = statusReason,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes
        )
    }

    private fun encodeRecord(record: DownloadRecord): String {
        return listOf(
            record.downloadId.toString(),
            record.title,
            record.sourceUrl,
            record.fileName,
            record.mimeType.orEmpty(),
            record.createdAtMillis.toString(),
            record.status.storageValue,
            record.statusReason?.toString().orEmpty(),
            record.bytesDownloaded?.toString().orEmpty(),
            record.totalBytes?.toString().orEmpty()
        ).joinToString(separator = "\t", transform = ::escape)
    }

    private fun splitEscaped(line: String): List<String> {
        val fields = mutableListOf<String>()
        val current = StringBuilder()
        var escaping = false
        line.forEach { char ->
            if (escaping) {
                current.append(
                    when (char) {
                        't' -> '\t'
                        'n' -> '\n'
                        'r' -> '\r'
                        else -> char
                    }
                )
                escaping = false
            } else {
                when (char) {
                    '\\' -> escaping = true
                    '\t' -> {
                        fields.add(current.toString())
                        current.clear()
                    }
                    else -> current.append(char)
                }
            }
        }
        if (escaping) {
            current.append('\\')
        }
        fields.add(current.toString())
        return fields
    }

    private fun escape(value: String): String {
        return buildString {
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\t' -> append("\\t")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    else -> append(char)
                }
            }
        }
    }

    private companion object {
        private const val KEY_DOWNLOAD_RECORDS = "download_records"
        private const val LEGACY_FIELD_COUNT = 6
        private const val STATUS_FIELD_COUNT = 7
        private const val STATUS_REASON_FIELD_COUNT = 8
        private const val FIELD_COUNT = 10
        private const val RECORD_LIMIT = 80
    }
}
