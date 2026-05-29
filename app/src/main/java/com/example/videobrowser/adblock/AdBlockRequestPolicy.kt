package com.example.videobrowser.adblock

import com.example.videobrowser.rules.RuleEngine

/**
 * 请求级广告拦截策略，先处理开关、主文档和协议边界，再进入规则系统匹配。
 */
object AdBlockRequestPolicy {
    fun evaluate(
        enabled: Boolean,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false,
        url: String,
        host: String?,
        pageHost: String? = null,
        scheme: String?,
        isForMainFrame: Boolean,
        ruleEngine: RuleEngine
    ): AdBlockDecision {
        if (!enabled) {
            return AdBlockDecision.allow(AdBlockDecisionReason.DISABLED)
        }
        if (isForMainFrame) {
            return AdBlockDecision.allow(AdBlockDecisionReason.MAIN_FRAME)
        }
        if (!isHttpScheme(scheme)) {
            return AdBlockDecision.allow(AdBlockDecisionReason.NON_HTTP_SCHEME)
        }
        if (userWhitelisted) {
            return AdBlockDecision.allow(AdBlockDecisionReason.USER_WHITELISTED)
        }
        // 当前站点关闭广告拦截时，只跳过请求级规则，不改变全局开关本身。
        if (siteAdBlockDisabled) {
            return AdBlockDecision.allow(AdBlockDecisionReason.SITE_AD_BLOCK_DISABLED)
        }

        val result = ruleEngine.matchRequest(url = url, host = host, pageHost = pageHost)
        if (result.shouldAllow) {
            return AdBlockDecision.allowByRule(result)
        }
        if (result.shouldBlock) {
            return AdBlockDecision.blockByRule(result)
        }
        return AdBlockDecision.allow(AdBlockDecisionReason.NO_MATCH)
    }

    fun shouldBlock(
        enabled: Boolean,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false,
        url: String,
        host: String?,
        pageHost: String? = null,
        scheme: String?,
        isForMainFrame: Boolean,
        ruleEngine: RuleEngine
    ): Boolean {
        return evaluate(
            enabled = enabled,
            siteAdBlockDisabled = siteAdBlockDisabled,
            userWhitelisted = userWhitelisted,
            url = url,
            host = host,
            pageHost = pageHost,
            scheme = scheme,
            isForMainFrame = isForMainFrame,
            ruleEngine = ruleEngine
        ).shouldBlock
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
