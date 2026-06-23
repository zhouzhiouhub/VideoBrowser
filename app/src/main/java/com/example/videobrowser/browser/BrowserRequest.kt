package com.example.videobrowser.browser

/**
 * 初学者阅读提示：
 * 这个文件属于“浏览器核心模块”。
 * 文件名 BrowserRequest 可以拆开理解为“Browser Request”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：封装 WebView 页面加载、标签页、导航安全、页面工具、权限回调或浏览器控件状态。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import android.net.Uri
import android.webkit.WebResourceRequest
import com.example.videobrowser.utils.AndroidUriParser

/**
 * 统一的 WebView 请求模型，供广告拦截和后续规则匹配使用。
 */
data class BrowserRequest(
    val url: Uri,
    val isForMainFrame: Boolean,
    val method: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
    val hasUserGesture: Boolean = false,
    val isRedirect: Boolean = false,
    val pageUrl: String? = null
) {
    companion object {
        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param request 参数类型为 `WebResourceRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
         * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun from(request: WebResourceRequest, pageUrl: String? = null): BrowserRequest {
            return BrowserRequest(
                url = request.url,
                isForMainFrame = request.isForMainFrame,
                method = request.method,
                requestHeaders = request.requestHeaders.orEmpty().toMap(),
                hasUserGesture = request.hasGesture(),
                isRedirect = request.isRedirect,
                pageUrl = pageUrl
            )
        }

        /**
         * 旧版 WebView 拦截回调只暴露 URL，缺失的请求字段保持默认值。
         */
        fun from(uri: Uri, isForMainFrame: Boolean = false, pageUrl: String? = null): BrowserRequest {
            return BrowserRequest(
                url = uri,
                isForMainFrame = isForMainFrame,
                pageUrl = pageUrl
            )
        }

        /**
         * 函数 `from`：封装 `from` 这一段业务步骤，让调用方不用关心内部实现细节。
         *
         * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
         * @param url 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @param isForMainFrame 参数类型为 `Boolean`，表示函数执行 `isForMainFrame` 相关逻辑时需要读取或处理的输入。
         * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
         * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
         */
        fun from(url: String?, isForMainFrame: Boolean = false, pageUrl: String? = null): BrowserRequest? {
            val uri = AndroidUriParser.parseTrimmedOrNull(url) ?: return null
            return from(uri, isForMainFrame, pageUrl)
        }
    }
}
