package com.example.videobrowser.adblock

/**
 * 初学者阅读提示：
 * 这个文件属于“广告请求拦截模块”。
 * 文件名 AdBlockManager 可以拆开理解为“Ad Block Manager”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：把 WebView 的网络请求交给规则系统判断，并在需要拦截时返回安全的本地响应。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.rules.RuleEngine

/**
 * 广告请求拦截管理入口，负责把请求级边界策略分发到规则系统。
 */
class AdBlockManager(
    private val isEnabled: () -> Boolean = { true },
    private val isDisabledForCurrentSite: () -> Boolean = { false },
    private val isUserWhitelistedRequestHost: (String?) -> Boolean = { false },
    private val currentPageUrl: () -> String? = { null },
    private val currentPageHost: () -> String? = { null },
    private val logger: AdBlockLogger? = null,
    private val ruleEngine: RuleEngine = RuleEngine(BuiltInAdBlockRules.requestRules())
) {
    fun evaluate(request: BrowserRequest): AdBlockDecision {
        // RequestContext 把 URL、页面 URL、host、资源类型等信息整理好，规则系统只依赖这个统一模型。
        val context = RequestContext.from(
            request = request,
            pageUrl = request.pageUrl ?: currentPageUrl()
        )
        val resolvedContext = context.copy(pageHost = context.pageHost ?: currentPageHost())
        val decision = AdBlockRequestPolicy.evaluate(
            enabled = isEnabled(),
            siteAdBlockDisabled = isDisabledForCurrentSite(),
            userWhitelisted = isUserWhitelistedRequestHost(context.requestHost),
            context = resolvedContext,
            ruleEngine = ruleEngine
        )
        // 只有命中规则或关键跳过原因才写日志，避免每个普通请求都刷屏。
        logDecision(request, resolvedContext, decision)
        return decision
    }

    fun shouldBlock(request: BrowserRequest): Boolean {
        return evaluate(request).shouldBlock
    }

    private fun logDecision(
        request: BrowserRequest,
        context: RequestContext,
        decision: AdBlockDecision
    ) {
        if (!decision.shouldLog) {
            return
        }
        logger?.log(
            action = if (decision.shouldBlock) AdBlockLogAction.BLOCK else AdBlockLogAction.ALLOW,
            url = request.url.toString(),
            host = request.url.host,
            decision = decision,
            pageHost = context.pageHost
        )
    }
}
