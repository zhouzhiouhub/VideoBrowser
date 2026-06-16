package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 RequestContext 可以拆开理解为“Request Context”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

/**
 * 规则匹配使用的请求上下文，集中保存请求、页面和轻量资源类型信息。
 */
data class RequestContext(
    val requestUrl: String,
    val pageUrl: String? = null,
    val requestHost: String? = normalizedHostFromUrl(requestUrl),
    val pageHost: String? = normalizedHostFromUrl(pageUrl),
    val requestScheme: String? = schemeFromUrl(requestUrl),
    val isForMainFrame: Boolean = false,
    val method: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
    val resourceType: ResourceType = ResourceTypeResolver.resolve(
        requestUrl = requestUrl,
        isForMainFrame = isForMainFrame,
        requestHeaders = requestHeaders
    )
) {
    companion object {
        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
         * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun from(
            request: BrowserRequest,
            pageUrl: String? = request.pageUrl
        ): RequestContext {
            val requestUrl = request.url.toString()
            return RequestContext(
                requestUrl = requestUrl,
                pageUrl = pageUrl,
                requestHost = normalizeHost(request.url.host) ?: normalizedHostFromUrl(requestUrl),
                pageHost = normalizedHostFromUrl(pageUrl),
                requestScheme = request.url.scheme ?: schemeFromUrl(requestUrl),
                isForMainFrame = request.isForMainFrame,
                method = request.method,
                requestHeaders = request.requestHeaders
            )
        }

        /**
         * 函数 `hostFromUrl`：封装 `host From Url` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun hostFromUrl(url: String?): String? {
            return normalizedHostFromUrl(url)
        }
    }
}

/**
 * 函数 `normalizedHostFromUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
 *
 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
 * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
 * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
 */
private fun normalizedHostFromUrl(url: String?): String? {
    val value = url?.trim().orEmpty()
    if (value.isEmpty()) {
        return null
    }
    return normalizeHost(runCatching { URI(value).host }.getOrNull())
}

/**
 * 函数 `normalizeHost`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
 *
 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
 * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
 * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
 */
private fun normalizeHost(host: String?): String? {
    return host
        ?.trim()
        ?.trim('.')
        ?.lowercase(Locale.US)
        ?.takeIf { it.isNotEmpty() }
}

/**
 * 函数 `schemeFromUrl`：封装 `scheme From Url` 这一段业务步骤，让调用方不用关心内部实现细节。
 *
 * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
 * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
 * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
 */
private fun schemeFromUrl(url: String?): String? {
    val value = url?.trim().orEmpty()
    if (value.isEmpty()) {
        return null
    }
    return runCatching { URI(value).scheme }
        .getOrNull()
        ?: value.substringBefore(':', missingDelimiterValue = "")
            .takeIf { it.isNotBlank() && it != value }
}
