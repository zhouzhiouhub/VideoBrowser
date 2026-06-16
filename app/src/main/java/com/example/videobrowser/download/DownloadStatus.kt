package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadStatus 可以拆开理解为“Download Status”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
enum class DownloadStatus(val storageValue: String) {
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELED("canceled");

    companion object {
        fun fromStorage(value: String): DownloadStatus? {
            val normalized = value.trim()
            return values().firstOrNull { status ->
                status.storageValue.equals(normalized, ignoreCase = true) ||
                    status.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}
