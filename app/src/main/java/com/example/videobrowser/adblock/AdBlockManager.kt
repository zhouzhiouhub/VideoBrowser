package com.example.videobrowser.adblock

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.rules.RuleEngine

/**
 * 广告请求拦截管理入口，负责把请求级边界策略分发到规则系统。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true },
    private val ruleEngine: RuleEngine = RuleEngine(BuiltInAdBlockRules.requestRules())
) {
    fun shouldBlock(request: BrowserRequest): Boolean {
        return AdBlockRequestPolicy.shouldBlock(
            enabled = isEnabled(),
            url = request.url.toString(),
            host = request.url.host,
            scheme = request.url.scheme,
            isForMainFrame = request.isForMainFrame,
            ruleEngine = ruleEngine
        )
    }
}
