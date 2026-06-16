package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadRecordSearch 可以拆开理解为“Download Record Search”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import java.util.Locale

object DownloadRecordSearch {
    fun filter(records: List<DownloadRecord>, query: String?): List<DownloadRecord> {
        val terms = query
            ?.trim()
            ?.lowercase(Locale.ROOT)
            ?.split(Regex("\\s+"))
            ?.filter { term -> term.isNotBlank() }
            ?: emptyList()
        if (terms.isEmpty()) {
            return records
        }

        return records.filter { record ->
            val haystack = listOf(
                record.title,
                record.fileName,
                record.sourceUrl,
                record.mimeType.orEmpty()
            ).joinToString(separator = "\n").lowercase(Locale.ROOT)
            terms.all { term -> haystack.contains(term) }
        }
    }
}
