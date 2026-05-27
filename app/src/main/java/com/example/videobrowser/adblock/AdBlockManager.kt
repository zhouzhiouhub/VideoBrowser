package com.example.videobrowser.adblock

import com.example.videobrowser.browser.BrowserRequest

/**
 * 广告请求拦截管理入口，当前只承接 P5 阶段的内置 URL 黑名单判断。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true }
) {
    fun shouldBlock(request: BrowserRequest): Boolean {
        // 主文档请求必须放行，避免广告规则误杀页面导航。
        if (!isEnabled() || request.isForMainFrame || !isHttpRequest(request)) {
            return false
        }

        return BuiltInAdBlockRules.matches(
            url = request.url.toString(),
            host = request.url.host
        )
    }

    private fun isHttpRequest(request: BrowserRequest): Boolean {
        val scheme = request.url.scheme
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }

}
