package com.example.videobrowser.utils

/**
 * 初学者阅读提示：
 * 这个文件属于“通用工具模块”。
 * 文件名 UrlUtils 可以拆开理解为“Url Utils”，表示它只负责应用管理或数据层中的一个小职责。
 * 主要职责：提供 URL、媒体地址等跨模块复用的纯函数。
 * 阅读顺序：先看构造参数和数据模型，再看公开函数如何被 MainActivity 或功能中心页面调用。
 */
/**
 * URL 解析和展示工具。
 *
 * 这里的函数都是纯函数：不访问 Android 系统，也不读写状态。
 * 地址栏、搜索建议、站点安全和历史展示都会复用这些规则。
 */
object UrlUtils {
    /**
     * 函数 `resolveAddressInput`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param input 参数类型为 `String`，表示函数执行 `input` 相关逻辑时需要读取或处理的输入。
     * @param searchUrlPrefix 参数类型为 `String`，表示函数执行 `searchUrlPrefix` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun resolveAddressInput(
        input: String,
        searchUrlPrefix: String
    ): String? {
        // 地址栏输入如果能解析成网页 URL 就直接打开，否则拼成当前搜索引擎的搜索 URL。
        val value = input.trim()
        if (value.isEmpty()) {
            return null
        }

        return UrlLoadableAddressResolver.resolveLoadableUrl(value)
            ?: "$searchUrlPrefix${encodeSearchQuery(value)}"
    }

    /**
     * 函数 `searchQueryFromUrl`：封装 `search Query From Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param searchUrlPrefix 参数类型为 `String`，表示函数执行 `searchUrlPrefix` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun searchQueryFromUrl(url: String, searchUrlPrefix: String): String? {
        return SearchUrlQueryParser.searchQueryFromUrl(url, searchUrlPrefix)
    }

    /**
     * 函数 `displayUrl`：封装 `display Url` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun displayUrl(url: String): String {
        return UrlDisplayFormatter.displayUrl(url)
    }

    /**
     * 函数 `encodeSearchQuery`：封装 `encode Search Query` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param value 参数类型为 `String`，表示参与计算或写入的数值，函数会据此更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun encodeSearchQuery(value: String): String {
        val query = value.replace(WHITESPACE_SEQUENCE, " ").trim()
        return Utf8UrlCodec.encodeFormComponent(query)
    }

    private val WHITESPACE_SEQUENCE = Regex("\\s+")
}
