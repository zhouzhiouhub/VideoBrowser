package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordFilter 可以拆开理解为“Download Record Filter”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
object DownloadRecordFilter {
    fun filter(
        records: List<DownloadRecord>,
        status: DownloadStatus? = null,
        category: DownloadCategory? = null
    ): List<DownloadRecord> {
        return records.filter { record ->
            val statusMatches = status == null || record.status == status
            val categoryMatches = category == null ||
                DownloadCategory.from(record.mimeType, record.fileName) == category
            statusMatches && categoryMatches
        }
    }
}
