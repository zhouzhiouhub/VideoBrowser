package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 RequestContext 可以拆开理解为“Request Context”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.utils.HostNameNormalizer
import java.net.URI

/**
 * 规则匹配使用的请求上下文，集中保存请求、页面和轻量资源类型信息。
 */
data class RequestContext(
    val requestUrl: String,
    val pageUrl: String? = null,
    val requestHost: String? = HostNameNormalizer.fromUrl(requestUrl),
    val pageHost: String? = HostNameNormalizer.fromUrl(pageUrl),
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
                requestHost = HostNameNormalizer.normalize(request.url.host)
                    ?: HostNameNormalizer.fromUrl(requestUrl),
                pageHost = HostNameNormalizer.fromUrl(pageUrl),
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
            return HostNameNormalizer.fromUrl(url)
        }
    }
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
