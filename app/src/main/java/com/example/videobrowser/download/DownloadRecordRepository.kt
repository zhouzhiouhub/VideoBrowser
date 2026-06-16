package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordRepository 可以拆开理解为“Download Record Repository”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.storage.PreferenceStore

/**
 * 下载记录仓库。
 *
 * Android DownloadManager 保存真实任务；这里保存应用自己的展示快照，
 * 包括文件名、来源 URL、状态、失败原因和进度字节数。
 */
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
        // 字段数量判断用于兼容旧版本记录：旧记录没有状态、原因或进度字段时仍能读取。
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
