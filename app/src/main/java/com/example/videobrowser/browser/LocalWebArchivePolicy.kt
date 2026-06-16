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

    /**
     * 函数 `isWebArchive`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param displayName 参数类型为 `String?`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param mimeType 参数类型为 `String?`，表示函数执行 `mimeType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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
