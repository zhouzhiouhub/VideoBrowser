package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadProgress 可以拆开理解为“Download Progress”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
data class DownloadProgress(
    val bytesDownloaded: Long?,
    val totalBytes: Long?
) {
    val hasDownloadedBytes: Boolean
        get() = bytesDownloaded != null && bytesDownloaded >= 0L

    val hasKnownTotal: Boolean
        get() = totalBytes != null && totalBytes > 0L

    /**
     * 函数 `percent`：封装 `percent` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun percent(): Int? {
        val downloaded = bytesDownloaded?.takeIf { it >= 0L } ?: return null
        val total = totalBytes?.takeIf { it > 0L } ?: return null
        return ((downloaded.coerceAtMost(total) * 100L) / total).toInt().coerceIn(0, 100)
    }
}
