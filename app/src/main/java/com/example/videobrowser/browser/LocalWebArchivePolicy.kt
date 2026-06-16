package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 LocalWebArchivePolicy 可以拆开理解为“Local Web Archive Policy”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
object LocalWebArchivePolicy {
    private val webArchiveExtensions = setOf("mht", "mhtml")
    private val webArchiveMimeTypes = setOf(
        "application/mhtml",
        "application/x-mht",
        "application/x-mhtml",
        "application/x-mimearchive",
        "multipart/related"
    )

    fun isWebArchive(displayName: String?, mimeType: String?): Boolean {
        val extension = displayName
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.lowercase()
            .orEmpty()
        val normalizedMimeType = mimeType
            ?.substringBefore(';')
            ?.trim()
            ?.lowercase()
            .orEmpty()

        return extension in webArchiveExtensions || normalizedMimeType in webArchiveMimeTypes
    }
}
