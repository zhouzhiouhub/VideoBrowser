package com.example.videobrowser.localfiles

/**
 * 初学者阅读提示：
 * 这个文件属于“本地文件模块”。
 * 文件名 LocalDocumentFormatter 可以拆开理解为“Local Document Formatter”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：管理目录授权、读取本地文档列表，并把本地媒体交给浏览器或播放器打开。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import android.content.Context
import com.example.videobrowser.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDocumentFormatter(
    private val context: Context
) {
    fun summary(document: LocalDocument): String {
        if (document.isDirectory) {
            return context.getString(R.string.local_file_type_folder)
        }

        val type = document.mimeType
            ?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.local_file_type_unknown)
        return listOf(
            type,
            formatFileSize(document.size),
            formatModifiedTime(document.modifiedAt)
        ).joinToString(separator = " · ")
    }

    private fun formatFileSize(size: Long?): String {
        if (size == null || size < 0) {
            return context.getString(R.string.local_file_size_unknown)
        }

        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var value = size.toDouble()
        var unitIndex = 0
        while (value >= 1024 && unitIndex < units.lastIndex) {
            value /= 1024
            unitIndex++
        }
        return if (unitIndex == 0) {
            "$size ${units[unitIndex]}"
        } else {
            String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
        }
    }

    private fun formatModifiedTime(modifiedAt: Long?): String {
        if (modifiedAt == null || modifiedAt <= 0L) {
            return context.getString(R.string.local_file_time_unknown)
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
    }
}
