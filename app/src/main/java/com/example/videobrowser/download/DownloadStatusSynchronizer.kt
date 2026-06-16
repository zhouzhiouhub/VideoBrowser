package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadStatusSynchronizer 可以拆开理解为“Download Status Synchronizer”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
data class DownloadStatusSnapshot(
    val status: DownloadStatus,
    val statusReason: Int? = null,
    val bytesDownloaded: Long? = null,
    val totalBytes: Long? = null
)

class DownloadStatusSynchronizer(
    private val repository: DownloadRecordRepository,
    private val querySnapshot: (Long) -> DownloadStatusSnapshot?
) {
    /**
     * 函数 `refresh`：根据最新状态刷新 `refresh` 相关数据或界面，让调用方看到一致结果。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param records 参数类型为 `List<DownloadRecord>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun refresh(records: List<DownloadRecord> = repository.records()): List<DownloadRecord> {
        records
            .filter { record -> record.status == DownloadStatus.IN_PROGRESS }
            .forEach { record ->
                val snapshot = querySnapshot(record.downloadId) ?: return@forEach
                repository.updateSnapshot(
                    downloadId = record.downloadId,
                    status = snapshot.status,
                    statusReason = snapshot.statusReason,
                    bytesDownloaded = snapshot.bytesDownloaded,
                    totalBytes = snapshot.totalBytes
                )
            }
        return repository.records()
    }
}
