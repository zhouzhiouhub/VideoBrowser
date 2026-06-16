package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadCanceller 可以拆开理解为“Download Canceller”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
data class DownloadCancellationResult(
    val requestedSystemDownloadCount: Int,
    val removedSystemDownloadCount: Int,
    val statusUpdated: Boolean
) {
    val canceled: Boolean
        get() = removedSystemDownloadCount > 0 && statusUpdated
}

class DownloadCanceller(
    private val downloadRecordRepository: DownloadRecordRepository,
    private val systemDownloadRemover: SystemDownloadRemover
) {
    /**
     * 函数 `cancel`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param record 参数类型为 `DownloadRecord`，表示函数执行 `record` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cancel(record: DownloadRecord): DownloadCancellationResult {
        if (!DownloadCancellationPolicy.canCancel(record)) {
            return DownloadCancellationResult(
                requestedSystemDownloadCount = 0,
                removedSystemDownloadCount = 0,
                statusUpdated = false
            )
        }

        val removedCount = runCatching {
            systemDownloadRemover.remove(longArrayOf(record.downloadId))
        }.getOrDefault(0).coerceAtLeast(0)
        val statusUpdated = if (removedCount > 0) {
            downloadRecordRepository.updateStatus(record.downloadId, DownloadStatus.CANCELED)
        } else {
            false
        }
        return DownloadCancellationResult(
            requestedSystemDownloadCount = 1,
            removedSystemDownloadCount = removedCount,
            statusUpdated = statusUpdated
        )
    }
}
