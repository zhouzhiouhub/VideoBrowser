package com.example.videobrowser.browser

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
