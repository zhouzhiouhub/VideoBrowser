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
        /**
         * 函数 `fromStorage`：封装 `from Storage` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun fromStorage(value: String): DownloadStatus? {
            val normalized = value.trim()
            return values().firstOrNull { status ->
                status.storageValue.equals(normalized, ignoreCase = true) ||
                    status.name.equals(normalized, ignoreCase = true)
            }
        }
    }
}
