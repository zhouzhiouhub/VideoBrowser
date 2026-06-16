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

        fun from(url: String?, isForMainFrame: Boolean = false, pageUrl: String? = null): BrowserRequest? {
            val value = url?.trim().orEmpty()
            if (value.isEmpty()) {
                return null
            }
            return from(Uri.parse(value), isForMainFrame, pageUrl)
        }
    }
}
