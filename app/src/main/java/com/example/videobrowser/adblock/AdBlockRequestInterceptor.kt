package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockRequestInterceptor 可以拆开理解为“Ad Block Request Interceptor”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
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
