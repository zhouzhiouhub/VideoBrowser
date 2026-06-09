package com.example.videobrowser.adblock

import android.webkit.WebResourceResponse
import com.example.videobrowser.browser.BrowserRequest

/**
 * 连接 WebView 请求、广告判断和空响应生成，供 BrowserClient 的请求拦截回调直接调用。
 */
class AdBlockRequestInterceptor(
    private val adBlockManager: AdBlockManager,
    private val syntheticResponseFactory: SyntheticResponseFactory = SyntheticResponseFactory()
) {
    fun intercept(request: BrowserRequest): WebResourceResponse? {
        val decision = adBlockManager.evaluate(request)
        if (!decision.shouldBlock) {
            return null
        }
        val redirectResourceName = decision.ruleMatchResult.rule?.redirectResourceName
        if (!request.isForMainFrame && redirectResourceName != null) {
            syntheticResponseFactory.create(redirectResourceName)?.let { response ->
                return response
            }
        }
        return EmptyResponseFactory.noContent()
    }
}
