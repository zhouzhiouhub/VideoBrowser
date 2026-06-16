package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 PageArchiveFileName 可以拆开理解为“Page Archive File Name”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI

object PageArchiveFileName {
    private const val MAX_BASE_NAME_LENGTH = 80
    private const val EXTENSION = ".mhtml"
    private val invalidFileNameCharacters = Regex("""[\u0000-\u001F\\/:*?"<>|]+""")
    private val whitespaceSequence = Regex("\\s+")

    fun create(pageTitle: String, pageUrl: String?, fallbackName: String): String {
        val baseName = listOf(
            sanitize(pageTitle),
            sanitize(hostFromUrl(pageUrl)),
            sanitize(fallbackName)
        ).firstOrNull { value -> value.isNotBlank() } ?: "page"

        return baseName
            .take(MAX_BASE_NAME_LENGTH)
            .trimEnd('.', ' ')
            .ifBlank { "page" } + EXTENSION
    }

    private fun hostFromUrl(pageUrl: String?): String {
        return runCatching {
            URI(pageUrl.orEmpty()).host.orEmpty()
        }.getOrDefault("")
    }

    private fun sanitize(value: String): String {
        return value
            .replace(whitespaceSequence, " ")
            .replace(invalidFileNameCharacters, "_")
            .trim(' ', '.', '_')
    }
}
