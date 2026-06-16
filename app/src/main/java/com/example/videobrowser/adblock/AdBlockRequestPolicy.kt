package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockRequestPolicy 可以拆开理解为“Ad Block Request Policy”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
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
        // 主文档请求不进入广告规则匹配，避免把整个网页当成广告资源误拦截。
        // 非 http/https 请求也不匹配，因为它们可能是 about、file、intent 等特殊协议。
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
