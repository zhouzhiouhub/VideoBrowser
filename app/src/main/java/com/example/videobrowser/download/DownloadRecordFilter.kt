package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordFilter 可以拆开理解为“Download Record Filter”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
object DownloadRecordFilter {
    /**
     * 函数 `filter`：封装 `filter` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param records 参数类型为 `List<DownloadRecord>`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param status 参数类型为 `DownloadStatus?`，表示函数执行 `status` 相关逻辑时需要读取或处理的输入。
     * @param category 参数类型为 `DownloadCategory?`，表示函数执行 `category` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
