package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordCleaner 可以拆开理解为“Download Record Cleaner”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
fun interface SystemDownloadRemover {
    /**
     * 函数 `remove`：封装 `remove` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param downloadIds 参数类型为 `LongArray`，表示函数执行 `downloadIds` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun remove(downloadIds: LongArray): Int
}

data class DownloadRecordClearResult(
    val requestedSystemDownloadCount: Int,
    val removedSystemDownloadCount: Int
)

class DownloadRecordCleaner(
    private val downloadRecordRepository: DownloadRecordRepository,
    private val systemDownloadRemover: SystemDownloadRemover
) {
    /**
     * 函数 `clearRecordsAndFiles`：封装 `clear Records And Files` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun clearRecordsAndFiles(): DownloadRecordClearResult {
        val downloadIds = downloadRecordRepository.records()
            .map { record -> record.downloadId }
            .distinct()
            .toLongArray()
        val removedCount = if (downloadIds.isEmpty()) {
            0
        } else {
            runCatching { systemDownloadRemover.remove(downloadIds) }.getOrDefault(0)
        }

        downloadRecordRepository.clear()
        return DownloadRecordClearResult(
            requestedSystemDownloadCount = downloadIds.size,
            removedSystemDownloadCount = removedCount
        )
    }
}
