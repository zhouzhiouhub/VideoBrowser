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
    /**
     * 函数 `evaluate`：封装 `evaluate` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `shouldBlock`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun shouldBlock(request: BrowserRequest): Boolean {
        return evaluate(request).shouldBlock
    }

    /**
     * 函数 `logDecision`：封装 `log Decision` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @param decision 参数类型为 `AdBlockDecision`，表示函数执行 `decision` 相关逻辑时需要读取或处理的输入。
     */
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
