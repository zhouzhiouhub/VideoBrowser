package com.example.videobrowser.browser

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
