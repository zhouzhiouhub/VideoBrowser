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
import com.example.videobrowser.utils.WebSchemePolicy

/**
 * 请求级广告拦截策略，先处理开关、主文档和协议边界，再进入规则系统匹配。
 */
object AdBlockRequestPolicy {
    private val decisionResolver = RuleDecisionResolver()

    /**
     * 函数 `evaluate`：封装 `evaluate` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param siteAdBlockDisabled 参数类型为 `Boolean`，表示函数执行 `siteAdBlockDisabled` 相关逻辑时需要读取或处理的输入。
     * @param userWhitelisted 参数类型为 `Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @param ruleEngine 参数类型为 `RuleEngine`，表示函数执行 `ruleEngine` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun evaluate(
        enabled: Boolean,
        siteAdBlockDisabled: Boolean = false,
        userWhitelisted: Boolean = false,
        context: RequestContext,
        ruleEngine: RuleEngine
    ): AdBlockDecision {
        // 主文档请求不进入广告规则匹配，避免把整个网页当成广告资源误拦截。
        // 非 http/https 请求也不匹配，因为它们可能是 about、file、intent 等特殊协议。
        val ruleSummary = if (
            enabled &&
            !context.isForMainFrame &&
            WebSchemePolicy.isHttpOrHttpsScheme(context.requestScheme)
        ) {
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

    /**
     * 函数 `evaluate`：封装 `evaluate` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param siteAdBlockDisabled 参数类型为 `Boolean`，表示函数执行 `siteAdBlockDisabled` 相关逻辑时需要读取或处理的输入。
     * @param userWhitelisted 参数类型为 `Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param scheme 参数类型为 `String?`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
     * @param isForMainFrame 参数类型为 `Boolean`，表示函数执行 `isForMainFrame` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @param ruleEngine 参数类型为 `RuleEngine`，表示函数执行 `ruleEngine` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

    /**
     * 函数 `shouldBlock`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param enabled 参数类型为 `Boolean`，表示一个开关状态，用来决定函数内部走启用还是停用分支。
     * @param siteAdBlockDisabled 参数类型为 `Boolean`，表示函数执行 `siteAdBlockDisabled` 相关逻辑时需要读取或处理的输入。
     * @param userWhitelisted 参数类型为 `Boolean`，表示一组待处理数据，函数会遍历、过滤或转换这些内容。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param scheme 参数类型为 `String?`，表示函数执行 `scheme` 相关逻辑时需要读取或处理的输入。
     * @param isForMainFrame 参数类型为 `Boolean`，表示函数执行 `isForMainFrame` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @param ruleEngine 参数类型为 `RuleEngine`，表示函数执行 `ruleEngine` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
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

}
