package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordRepository 可以拆开理解为“Download Record Repository”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import com.example.videobrowser.settings.PreferenceLineStore
import com.example.videobrowser.settings.TabSeparatedLineCodec
import com.example.videobrowser.storage.PreferenceStore

/**
 * 下载记录仓库。
 *
 * Android DownloadManager 保存真实任务；这里保存应用自己的展示快照，
 * 包括文件名、来源 URL、状态、失败原因和进度字节数。
 */
class DownloadRecordRepository(
    preferenceStore: PreferenceStore
) {
    private val lineStore = PreferenceLineStore(preferenceStore, KEY_DOWNLOAD_RECORDS)

    /**
     * 函数 `add`：封装 `add` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     */
    fun add(record: DownloadRecord) {
        val records = records()
            .filterNot { item -> item.downloadId == record.downloadId }
            .toMutableList()
        records.add(0, record)
        save(records.take(RECORD_LIMIT))
    }

    /**
     * 函数 `updateStatus`：根据最新状态刷新 `update Status` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @param status 参数类型为 `DownloadStatus`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @param statusReason 参数类型为 `Int?`，表示函数执行 `statusReason` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `updateSnapshot`：根据最新状态刷新 `update Snapshot` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @param status 参数类型为 `DownloadStatus`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @param statusReason 参数类型为 `Int?`，表示函数执行 `statusReason` 相关逻辑时需要读取或处理的输入。
     * @param bytesDownloaded 参数类型为 `Long?`，表示函数执行 `bytesDownloaded` 相关逻辑时需要读取或处理的输入。
     * @param totalBytes 参数类型为 `Long?`，表示函数执行 `totalBytes` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `records`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun records(): List<DownloadRecord> {
        return lineStore
            .loadLines()
            .mapNotNull(::parseRecord)
            .toList()
    }

    /**
     * 函数 `contains`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun contains(downloadId: Long): Boolean {
        return records().any { record -> record.downloadId == downloadId }
    }

    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadId 参数类型为 `Long`，表示函数执行 `downloadId` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun remove(downloadId: Long): Boolean {
        val existingRecords = records()
        val updatedRecords = existingRecords.filterNot { record -> record.downloadId == downloadId }
        if (updatedRecords.size == existingRecords.size) {
            return false
        }
        save(updatedRecords)
        return true
    }

    /**
     * 函数 `clear`：封装 `clear` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     */
    fun clear() {
        lineStore.clear()
    }

    /**
     * 函数 `save`：把传入数据写入内存、配置或持久化存储，并保持相关状态一致。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param records 参数类型为 `List<DownloadRecord>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     */
    private fun save(records: List<DownloadRecord>) {
        lineStore.saveLines(records.map(::encodeRecord))
    }

    /**
     * 函数 `parseRecord`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param line 参数类型为 `String`，表示函数执行 `line` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun parseRecord(line: String): DownloadRecord? {
        // 字段数量判断用于兼容旧版本记录：旧记录没有状态、原因或进度字段时仍能读取。
        val fields = TabSeparatedLineCodec.splitFields(line)
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

    /**
     * 函数 `encodeRecord`：封装 `encode Record` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun encodeRecord(record: DownloadRecord): String {
        return TabSeparatedLineCodec.joinFields(
            listOf(
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
            )
        )
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
