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
import com.example.videobrowser.utils.ByteSizeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocalDocumentFormatter(
    private val context: Context
) {
    /**
     * 函数 `summary`：封装 `summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param document 参数类型为 `LocalDocument`，表示函数执行 `document` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `formatFileSize`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param size 参数类型为 `Long?`，表示函数执行 `size` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun formatFileSize(size: Long?): String {
        if (size == null || size < 0) {
            return context.getString(R.string.local_file_size_unknown)
        }
        return ByteSizeFormatter.format(size, locale = Locale.getDefault())
    }

    /**
     * 函数 `formatModifiedTime`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param modifiedAt 参数类型为 `Long?`，表示函数执行 `modifiedAt` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun formatModifiedTime(modifiedAt: Long?): String {
        if (modifiedAt == null || modifiedAt <= 0L) {
            return context.getString(R.string.local_file_time_unknown)
        }
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(modifiedAt))
    }
}
