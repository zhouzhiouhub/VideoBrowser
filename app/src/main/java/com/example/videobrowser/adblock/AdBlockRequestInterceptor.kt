package com.example.videobrowser.adblock

import android.webkit.WebResourceResponse
import com.example.videobrowser.browser.BrowserRequest

/**
 * 连接 WebView 请求、广告判断和空响应生成，供 BrowserClient 的请求拦截回调直接调用。
 */
class AdBlockRequestInterceptor(
    private val adBlockManager: AdBlockManager
) {
    fun intercept(request: BrowserRequest): WebResourceResponse? {
        return if (adBlockManager.shouldBlock(request)) {
            EmptyResponseFactory.noContent()
        } else {
            null
        }
    }
}
