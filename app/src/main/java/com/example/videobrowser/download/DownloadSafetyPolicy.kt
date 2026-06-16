package com.example.videobrowser.download

/**
 * 初学者阅读提示：
 * 这个文件属于“下载管理模块”。
 * 文件名 DownloadSafetyPolicy 可以拆开理解为“Download Safety Policy”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：创建下载任务、记录下载状态、支持重试/取消/清理和下载列表过滤。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
import java.net.URI
import java.util.Locale

/**
 * 下载前的安全判断。
 *
 * 这里不创建下载任务，只回答三个问题：是否需要确认、URL 是否可下载、文件名是否安全。
 */
object DownloadSafetyPolicy {
    private const val DEFAULT_DOWNLOAD_FILE_NAME = "download.bin"
    private const val MAX_DOWNLOAD_FILE_NAME_LENGTH = 120
    private val invalidDownloadFileNameChars = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")

    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }

    fun requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && schemeOf(downloadUrl) == "http"
    }

    fun isDownloadableNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        return (scheme == "http" || scheme == "https") &&
            !uri.host.isNullOrBlank()
    }

    fun safeDownloadFileName(fileName: String): String {
        // 文件名会落到公共下载目录，必须移除路径分隔符、控制字符和 Windows 不允许的字符。
        val sanitized = fileName
            .trim()
            .replace(invalidDownloadFileNameChars, "_")
            .replace(Regex("\\s+"), " ")
            .trim('.', ' ')
            .take(MAX_DOWNLOAD_FILE_NAME_LENGTH)
            .trim('.', ' ')
        return sanitized.ifBlank { DEFAULT_DOWNLOAD_FILE_NAME }
    }

    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
