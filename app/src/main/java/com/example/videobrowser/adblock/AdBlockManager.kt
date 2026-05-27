package com.example.videobrowser.adblock

import com.example.videobrowser.browser.BrowserRequest

/**
 * 广告请求拦截管理入口，当前只承接 P5 阶段的内置 URL 黑名单判断。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true }
) {
    fun shouldBlock(request: BrowserRequest): Boolean {
        return AdBlockRequestPolicy.shouldBlock(
            enabled = isEnabled(),
            url = request.url.toString(),
            host = request.url.host,
            scheme = request.url.scheme,
            isForMainFrame = request.isForMainFrame
        )
    }
}
