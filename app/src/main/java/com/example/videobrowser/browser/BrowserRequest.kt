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
    val isRedirect: Boolean = false
) {
    companion object {
        fun from(request: WebResourceRequest): BrowserRequest {
            return BrowserRequest(
                url = request.url,
                isForMainFrame = request.isForMainFrame,
                method = request.method,
                requestHeaders = request.requestHeaders.orEmpty().toMap(),
                hasUserGesture = request.hasGesture(),
                isRedirect = request.isRedirect
            )
        }

        /**
         * 旧版 WebView 拦截回调只暴露 URL，缺失的请求字段保持默认值。
         */
        fun from(uri: Uri, isForMainFrame: Boolean = false): BrowserRequest {
            return BrowserRequest(
                url = uri,
                isForMainFrame = isForMainFrame
            )
        }

        fun from(url: String?, isForMainFrame: Boolean = false): BrowserRequest? {
            val value = url?.trim().orEmpty()
            if (value.isEmpty()) {
                return null
            }
            return from(Uri.parse(value), isForMainFrame)
        }
    }
}
