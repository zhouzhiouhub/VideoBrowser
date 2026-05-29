package com.example.videobrowser.adblock

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.rules.RuleEngine

/**
 * 广告请求拦截管理入口，负责把请求级边界策略分发到规则系统。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true },
    private val isDisabledForCurrentSite: () -> Boolean = { false },
    private val isUserWhitelistedRequestHost: (String?) -> Boolean = { false },
    private val currentPageHost: () -> String? = { null },
    private val logger: AdBlockLogger? = null,
    private val ruleEngine: RuleEngine = RuleEngine(BuiltInAdBlockRules.requestRules())
) {
    fun evaluate(request: BrowserRequest): AdBlockDecision {
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = isEnabled(),
            siteAdBlockDisabled = isDisabledForCurrentSite(),
            userWhitelisted = isUserWhitelistedRequestHost(request.url.host),
            url = request.url.toString(),
            host = request.url.host,
            pageHost = currentPageHost(),
            scheme = request.url.scheme,
            isForMainFrame = request.isForMainFrame,
            ruleEngine = ruleEngine
        )
        logDecision(request, decision)
        return decision
    }

    fun shouldBlock(request: BrowserRequest): Boolean {
        return evaluate(request).shouldBlock
    }

    private fun logDecision(request: BrowserRequest, decision: AdBlockDecision) {
        if (!decision.shouldLog) {
            return
        }
        logger?.log(
            action = if (decision.shouldBlock) AdBlockLogAction.BLOCK else AdBlockLogAction.ALLOW,
            url = request.url.toString(),
            host = request.url.host,
            decision = decision
        )
    }
}
