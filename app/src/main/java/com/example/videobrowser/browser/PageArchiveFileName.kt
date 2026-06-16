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

    /**
     * 函数 `create`：创建 `create` 需要的对象、视图或配置，并返回给后续流程使用。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageTitle 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param fallbackName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `hostFromUrl`：封装 `host From Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun hostFromUrl(pageUrl: String?): String {
        return runCatching {
            URI(pageUrl.orEmpty()).host.orEmpty()
        }.getOrDefault("")
    }

    /**
     * 函数 `sanitize`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun sanitize(value: String): String {
        return value
            .replace(whitespaceSequence, " ")
            .replace(invalidFileNameCharacters, "_")
            .trim(' ', '.', '_')
    }
}
