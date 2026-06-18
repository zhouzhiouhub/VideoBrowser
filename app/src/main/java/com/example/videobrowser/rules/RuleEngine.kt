package com.example.videobrowser.rules

/**
 * 初学者阅读提示：
 * 这个文件属于“规则引擎模块”。
 * 文件名 RuleEngine 可以拆开理解为“Rule Engine”，表示它只负责页面净化链路中的一个小职责。
 * 主要职责：读取、解析、索引和匹配广告拦截规则、元素隐藏规则、参数清理规则和安全脚本规则。
 * 阅读顺序：先看数据类/策略类表达什么规则，再看控制器如何把规则接到 WebView 请求或页面脚本上。
 */
import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.browser.ResourceType

/**
 * 页面净化规则的统一查询入口。
 *
 * RuleEngine 启动时先把原始规则编译成索引，运行时只回答几个问题：
 * - 某个网络请求是否应该放行或拦截。
 * - 当前页面应该隐藏/移除哪些 DOM 元素。
 * - 当前页面应该启用哪些安全脚本钩子。
 * - 导航 URL 上哪些追踪参数可以删除。
 */
class RuleEngine(
    rules: List<Rule>,
    elementRules: List<ElementRule> = emptyList(),
    scriptletRules: List<ScriptletRule> = emptyList(),
    removeParamRules: List<RemoveParamRule> = emptyList(),
    private val ruleMatcher: RuleMatcher = RuleMatcher(),
    ruleCompiler: RuleCompiler = RuleCompiler()
) {
    // G2-03 起请求匹配从编译产物索引取候选规则，未索引规则仍由 fallback 保持兼容。
    private val compiledRules = ruleCompiler.compile(
        requestRules = rules,
        elementRules = elementRules,
        scriptletRules = scriptletRules,
        removeParamRules = removeParamRules
    )
    private val requestQuery = RuleRequestQuery(compiledRules, ruleMatcher)
    private val elementSelectorQuery = RuleElementSelectorQuery(compiledRules)
    private val scriptletQuery = RuleScriptletQuery(compiledRules)

    /**
     * 函数 `matchRequest`：封装 `match Request` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param request 参数类型为 `BrowserRequest`，表示一次请求或响应，函数会检查它的内容并决定如何继续处理。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequest(request: BrowserRequest): RuleMatchResult {
        return matchRequest(RequestContext.from(request))
    }

    /**
     * 函数 `matchRequest`：封装 `match Request` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequest(context: RequestContext): RuleMatchResult {
        return requestQuery.matchRequest(context)
    }

    /**
     * 函数 `matchRequest`：封装 `match Request` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequest(
        url: String,
        host: String? = null,
        pageHost: String? = null,
        resourceType: ResourceType = ResourceType.UNKNOWN
    ): RuleMatchResult {
        return requestQuery.matchRequest(
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )
    }

    /**
     * 函数 `matchRequestCandidates`：封装 `match Request Candidates` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequestCandidates(context: RequestContext): List<RuleMatchResult> {
        return requestQuery.matchRequestCandidates(context)
    }

    /**
     * 函数 `matchRequestCandidates`：封装 `match Request Candidates` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequestCandidates(
        url: String,
        host: String? = null,
        pageHost: String? = null,
        resourceType: ResourceType = ResourceType.UNKNOWN
    ): List<RuleMatchResult> {
        return requestQuery.matchRequestCandidates(
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )
    }

    /**
     * 函数 `matchRequestSummary`：封装 `match Request Summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequestSummary(context: RequestContext): RequestRuleMatchSummary {
        return requestQuery.matchRequestSummary(context)
    }

    /**
     * 函数 `rules`：封装 `rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun rules(): List<Rule> {
        return compiledRules.requestRules()
    }

    /**
     * 函数 `elementRules`：封装 `element Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun elementRules(): List<ElementRule> {
        return compiledRules.elementRules()
    }

    /**
     * 函数 `skippedRules`：封装 `skipped Rules` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun skippedRules(): List<SkippedRule> {
        return compiledRules.skippedRules
    }

    /**
     * 函数 `cleanNavigationUrl`：把输入内容转换成更适合业务使用的格式，减少调用方重复处理细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cleanNavigationUrl(url: String, pageUrl: String? = null): String {
        return RuleNavigationUrlCleaner.clean(
            url = url,
            pageUrl = pageUrl,
            removeParamCapabilities = compiledRules.removeParamCapabilities,
            ruleMatcher = ruleMatcher
        )
    }

    /**
     * 函数 `cssSelectorsFor`：封装 `css Selectors For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssSelectorsFor(pageUrl: String?): List<String> {
        return elementSelectorQuery.cssSelectorsFor(pageUrl)
    }

    /**
     * 函数 `domSelectorsFor`：封装 `dom Selectors For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domSelectorsFor(pageUrl: String?): List<String> {
        return elementSelectorQuery.domSelectorsFor(pageUrl)
    }

    /**
     * 函数 `scriptletWindowOpenBlockedKeywordsFor`：封装 `scriptlet Window Open Blocked Keywords For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun scriptletWindowOpenBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletQuery.windowOpenBlockedKeywordsFor(pageUrl)
    }

    /**
     * 函数 `scriptletFetchBlockedKeywordsFor`：封装 `scriptlet Fetch Blocked Keywords For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun scriptletFetchBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletQuery.fetchBlockedKeywordsFor(pageUrl)
    }

    /**
     * 函数 `isScriptletSkipButtonsEnabledFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isScriptletSkipButtonsEnabledFor(pageUrl: String?): Boolean {
        return scriptletQuery.isSkipButtonsEnabledFor(pageUrl)
    }

    /**
     * 函数 `isScriptletVideoControlsEnabledFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isScriptletVideoControlsEnabledFor(pageUrl: String?): Boolean {
        return scriptletQuery.isVideoControlsEnabledFor(pageUrl)
    }

    /**
     * 函数 `urlContainsBlockPatternsFor`：封装 `url Contains Block Patterns For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun urlContainsBlockPatternsFor(pageUrl: String?): List<String> {
        return requestQuery.urlContainsBlockPatternsFor(pageUrl)
    }

}
