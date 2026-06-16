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

        fun hostFromUrl(url: String?): String? {
            return normalizedHostFromUrl(url)
        }
    }
}

private fun normalizedHostFromUrl(url: String?): String? {
    val value = url?.trim().orEmpty()
    if (value.isEmpty()) {
        return null
    }
    return normalizeHost(runCatching { URI(value).host }.getOrNull())
}

private fun normalizeHost(host: String?): String? {
    return host
        ?.trim()
        ?.trim('.')
        ?.lowercase(Locale.US)
        ?.takeIf { it.isNotEmpty() }
}

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
