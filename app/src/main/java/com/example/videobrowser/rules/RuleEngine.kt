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
import com.example.videobrowser.site.SiteHost
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
    private val requestCapabilities = compiledRules.requestCapabilities
    private val requestRuleOrder = requestCapabilities
        .mapIndexed { index, capability -> capability.rule to index }
        .toMap()

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
        return matchRequest(
            url = context.requestUrl,
            host = context.requestHost,
            pageHost = context.pageHost,
            resourceType = context.resourceType
        )
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
        // 放行规则优先于阻断规则，避免白名单被后面的通配拦截规则覆盖。
        findFirstMatchingRule(
            action = RuleAction.ALLOW,
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )?.let { allowRule ->
            return RuleMatchResult.allow(allowRule)
        }

        findFirstMatchingRule(
            action = RuleAction.BLOCK,
            url = url,
            host = host,
            pageHost = pageHost,
            resourceType = resourceType
        )?.let { blockRule ->
            return RuleMatchResult.block(blockRule)
        }

        return RuleMatchResult.NoMatch
    }

    /**
     * 函数 `matchRequestCandidates`：封装 `match Request Candidates` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequestCandidates(context: RequestContext): List<RuleMatchResult> {
        return matchRequestCandidates(
            url = context.requestUrl,
            host = context.requestHost,
            pageHost = context.pageHost,
            resourceType = context.resourceType
        )
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
        val requestHost = host ?: SiteHost.fromUrl(url)
        return (matchingCapabilitiesFor(RuleAction.ALLOW, url, requestHost, pageHost, resourceType) +
            matchingCapabilitiesFor(RuleAction.BLOCK, url, requestHost, pageHost, resourceType))
            .distinctBy { capability -> requestRuleOrder[capability.rule] ?: Int.MAX_VALUE }
            .sortedBy { capability -> requestRuleOrder[capability.rule] ?: Int.MAX_VALUE }
            .map { capability ->
                when (capability.rule.action) {
                    RuleAction.ALLOW -> RuleMatchResult.allow(capability.rule)
                    RuleAction.BLOCK -> RuleMatchResult.block(capability.rule)
                    RuleAction.NONE -> RuleMatchResult.NoMatch
                }
            }
            .filter { result -> result.matched }
    }

    /**
     * 函数 `matchRequestSummary`：封装 `match Request Summary` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param context 参数类型为 `RequestContext`，表示 Android 上下文，用来读取资源、启动系统服务或访问应用环境。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun matchRequestSummary(context: RequestContext): RequestRuleMatchSummary {
        val candidates = matchRequestCandidates(context)
        return RequestRuleMatchSummary(
            allowMatch = candidates.firstOrNull { result -> result.shouldAllow }
                ?: RuleMatchResult.NoMatch,
            blockMatch = candidates.firstOrNull { result -> result.shouldBlock }
                ?: RuleMatchResult.NoMatch,
            ruleCandidates = candidates
        )
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
        // 导航清理只处理 http/https URL，其他 scheme 不应该被当成普通网页参数改写。
        val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return url
        val scheme = uri.scheme?.lowercase().orEmpty()
        if (scheme != "http" && scheme != "https") {
            return url
        }
        val rawQuery = uri.rawQuery ?: return url
        if (rawQuery.isEmpty()) {
            return url
        }

        val requestHost = SiteHost.fromUrl(url)
        val pageHost = SiteHost.fromUrl(pageUrl)
        val parametersToRemove = compiledRules.removeParamCapabilities
            .filter { capability ->
                ruleMatcher.matches(
                    rule = capability.rule.toRequestMatcherRule(),
                    url = url,
                    host = requestHost,
                    pageHost = pageHost,
                    resourceType = ResourceType.DOCUMENT
                )
            }
            .map { capability -> capability.rule.parameterName }
            .toSet()
        if (parametersToRemove.isEmpty()) {
            return url
        }

        val queryParts = rawQuery.split("&")
        val keptQueryParts = queryParts.filterNot { part ->
            decodedQueryName(part.substringBefore("=")) in parametersToRemove
        }
        if (keptQueryParts.size == queryParts.size) {
            return url
        }
        return renderUriWithQuery(uri, keptQueryParts.joinToString("&").takeIf { it.isNotEmpty() })
    }

    /**
     * 函数 `cssSelectorsFor`：封装 `css Selectors For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun cssSelectorsFor(pageUrl: String?): List<String> {
        // 先找例外规则，再从隐藏规则里排除例外 selector，这就是常见过滤规则里的“取消隐藏”语义。
        val pageHost = SiteHost.fromUrl(pageUrl)
        val exceptions = compiledRules.cssUnhideCandidatesFor(pageHost)
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .toSet()
        return compiledRules.cssHideCandidatesFor(pageHost)
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .filterNot { selector -> selector in exceptions }
            .distinct()
    }

    /**
     * 函数 `domSelectorsFor`：封装 `dom Selectors For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun domSelectorsFor(pageUrl: String?): List<String> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.domRemoveCandidatesFor(pageHost)
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .distinct()
    }

    /**
     * 函数 `scriptletWindowOpenBlockedKeywordsFor`：封装 `scriptlet Window Open Blocked Keywords For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun scriptletWindowOpenBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletArgumentsFor(
            pageUrl = pageUrl,
            hookName = "window-open-block-keyword"
        )
    }

    /**
     * 函数 `scriptletFetchBlockedKeywordsFor`：封装 `scriptlet Fetch Blocked Keywords For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun scriptletFetchBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletArgumentsFor(
            pageUrl = pageUrl,
            hookName = "fetch-block-keyword"
        )
    }

    /**
     * 函数 `isScriptletSkipButtonsEnabledFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isScriptletSkipButtonsEnabledFor(pageUrl: String?): Boolean {
        return scriptletHooksFor(pageUrl).any { capability ->
            capability.hookName == "click-skip-buttons"
        }
    }

    /**
     * 函数 `isScriptletVideoControlsEnabledFor`：根据当前对象和传入参数计算布尔判断结果，调用方会用这个结果决定后续分支。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun isScriptletVideoControlsEnabledFor(pageUrl: String?): Boolean {
        return scriptletHooksFor(pageUrl).any { capability ->
            capability.hookName == "enable-video-controls"
        }
    }

    /**
     * 函数 `urlContainsBlockPatternsFor`：封装 `url Contains Block Patterns For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    fun urlContainsBlockPatternsFor(pageUrl: String?): List<String> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return requestCapabilities
            .map { capability -> capability.rule }
            .filter { rule ->
                rule.action == RuleAction.BLOCK &&
                    rule.type == RuleType.URL_CONTAINS &&
                    rule.thirdParty == null &&
                    rule.resourceTypes.isEmpty() &&
                    rule.domainScope.matches(pageHost)
            }
            .map { rule -> rule.pattern }
            .distinct()
    }

    /**
     * 函数 `decodedQueryName`：封装 `decoded Query Name` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param rawName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun decodedQueryName(rawName: String): String {
        return runCatching {
            URLDecoder.decode(rawName, StandardCharsets.UTF_8.name())
        }.getOrDefault(rawName)
    }

    /**
     * 函数 `renderUriWithQuery`：封装 `render Uri With Query` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param uri 参数类型为 `URI`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param rawQuery 参数类型为 `String?`，表示函数执行 `rawQuery` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun renderUriWithQuery(uri: URI, rawQuery: String?): String {
        return buildString {
            append(uri.scheme)
            append(":")
            uri.rawAuthority?.let { authority ->
                append("//")
                append(authority)
            }
            append(uri.rawPath.orEmpty())
            rawQuery?.let { query ->
                append("?")
                append(query)
            }
            uri.rawFragment?.let { fragment ->
                append("#")
                append(fragment)
            }
        }
    }

    /**
     * 函数 `findFirstMatchingRule`：从现有状态、缓存或输入对象中取得目标数据，并把结果交给调用方继续处理。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun findFirstMatchingRule(
        action: RuleAction,
        url: String,
        host: String?,
        pageHost: String?,
        resourceType: ResourceType
    ): Rule? {
        val requestHost = if (host != null) {
            host
        } else {
            SiteHost.fromUrl(url)
        }
        return matchingCapabilitiesFor(
            action = action,
            url = url,
            host = requestHost,
            pageHost = pageHost,
            resourceType = resourceType
        ).firstOrNull()?.rule
    }

    /**
     * 函数 `matchingCapabilitiesFor`：封装 `matching Capabilities For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param action 参数类型为 `RuleAction`，表示函数执行 `action` 相关逻辑时需要读取或处理的输入。
     * @param url 参数类型为 `String`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param host 参数类型为 `String?`，表示函数执行 `host` 相关逻辑时需要读取或处理的输入。
     * @param pageHost 参数类型为 `String?`，表示函数执行 `pageHost` 相关逻辑时需要读取或处理的输入。
     * @param resourceType 参数类型为 `ResourceType`，表示函数执行 `resourceType` 相关逻辑时需要读取或处理的输入。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun matchingCapabilitiesFor(
        action: RuleAction,
        url: String,
        host: String?,
        pageHost: String?,
        resourceType: ResourceType
    ): List<RuleCapability.Request> {
        return compiledRules.requestCandidatesFor(
            action = action,
            host = host,
            url = url
        ).filter { capability ->
            val rule = capability.rule
            ruleMatcher.matches(
                rule = rule,
                url = url,
                host = host,
                pageHost = pageHost,
                resourceType = resourceType
            )
            }
    }

    /**
     * 函数 `scriptletArgumentsFor`：封装 `scriptlet Arguments For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @param hookName 参数类型为 `String`，表示名称或键值，用来定位数据、生成展示文本或写入配置。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptletArgumentsFor(pageUrl: String?, hookName: String): List<String> {
        return scriptletHooksFor(pageUrl)
            .filter { capability -> capability.hookName == hookName }
            .flatMap { capability -> capability.arguments }
            .distinct()
    }

    /**
     * 函数 `scriptletHooksFor`：封装 `scriptlet Hooks For` 这一段业务步骤，让调用方不用关心内部实现细节。
     *
     * 初学者阅读提示：先看参数说明，再看函数体如何读取这些参数、更新状态或返回结果。
     * @param pageUrl 参数类型为 `String?`，表示要处理的地址，用来加载网页、匹配规则或展示给用户。
     * @return 返回函数处理后的结果；调用方会根据这个值继续后续流程。
     */
    private fun scriptletHooksFor(pageUrl: String?): List<RuleCapability.SafeHook> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.safeHookCapabilities.filter { capability ->
            capability.domainScope.matches(pageHost)
        }
    }
}
