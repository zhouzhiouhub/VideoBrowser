package com.example.videobrowser.adblock

import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.rules.RequestRuleMatchSummary
import com.example.videobrowser.rules.RuleEngine

/**
 * 请求级广告拦截策略，先处理开关、主文档和协议边界，再进入规则系统匹配。
 */
object AdBlockRequestPolicy {
    private val decisionResolver = RuleDecisionResolver()

    fun evaluate(
        enabled: Boolean,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false,
        context: RequestContext,
        ruleEngine: RuleEngine
    ): AdBlockDecision {
        val ruleSummary = if (enabled && !context.isForMainFrame && isHttpScheme(context.requestScheme)) {
            ruleEngine.matchRequestSummary(context)
        } else {
            RequestRuleMatchSummary.NoMatch
        }
        return decisionResolver.resolve(
            RuleDecisionResolver.Input(
                enabled = enabled,
                userWhitelisted = userWhitelisted,
                siteAdBlockDisabled = siteAdBlockDisabled,
                context = context,
                ruleSummary = ruleSummary
            )
        )
    }

    fun evaluate(
        enabled: Boolean,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false,
        url: String,
        host: String?,
        pageHost: String? = null,
        scheme: String?,
        isForMainFrame: Boolean,
        resourceType: ResourceType = ResourceType.UNKNOWN,
        ruleEngine: RuleEngine
    ): AdBlockDecision {
        val context = RequestContext(
            requestUrl = url,
            requestHost = host,
            pageHost = pageHost,
            requestScheme = scheme,
            isForMainFrame = isForMainFrame,
            resourceType = resourceType
        )
        return evaluate(
            enabled = enabled,
            siteAdBlockDisabled = siteAdBlockDisabled,
            userWhitelisted = userWhitelisted,
            context = context,
            ruleEngine = ruleEngine
        )
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
        resourceType: ResourceType = ResourceType.UNKNOWN,
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
            resourceType = resourceType,
            ruleEngine = ruleEngine
        ).shouldBlock
    }

    private fun isHttpScheme(scheme: String?): Boolean {
        return scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)
    }
}
