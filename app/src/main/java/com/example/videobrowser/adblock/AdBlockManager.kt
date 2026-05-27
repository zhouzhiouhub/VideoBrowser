package com.example.videobrowser.adblock

import com.example.videobrowser.browser.BrowserRequest

/**
 * 广告请求拦截管理入口，当前只承接 P5 阶段的内置关键字判断。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true }
) {
    fun shouldBlock(request: BrowserRequest): Boolean {
        // 主文档请求必须放行，避免广告规则误杀页面导航。
        if (!isEnabled() || request.isForMainFrame || !isHttpRequest(request)) {
            return false
        }

        val host = request.url.host?.lowercase().orEmpty()
        val url = request.url.toString().lowercase()
        return BLOCKED_AD_HOST_KEYWORDS.any { host.contains(it) } ||
            BLOCKED_AD_URL_KEYWORDS.any { url.contains(it) }
    }

    private fun isHttpRequest(request: BrowserRequest): Boolean {
        val scheme = request.url.scheme
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }

    private companion object {
        val BLOCKED_AD_HOST_KEYWORDS = listOf(
            "doubleclick.net",
            "googlesyndication.com",
            "googleadservices.com",
            "adservice.google.com",
            "googleads.g.doubleclick.net",
            "adnxs.com",
            "adsystem.com",
            "taboola.com",
            "outbrain.com",
            "ads-twitter.com",
            "analytics.yahoo.com"
        )
        val BLOCKED_AD_URL_KEYWORDS = listOf(
            "/pagead/",
            "/adservice/",
            "/adserver/",
            "/advert/",
            "/ads/",
            "/adx/",
            "googleads",
            "doubleclick",
            "ad.m3u8",
            "vast",
            "vmap",
            "preroll",
            "midroll"
        )
    }
}
