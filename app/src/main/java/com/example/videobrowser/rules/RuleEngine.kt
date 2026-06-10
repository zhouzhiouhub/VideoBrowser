package com.example.videobrowser.rules

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.site.SiteHost
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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

    fun matchRequest(request: BrowserRequest): RuleMatchResult {
        return matchRequest(RequestContext.from(request))
    }

    fun matchRequest(context: RequestContext): RuleMatchResult {
        return matchRequest(
            url = context.requestUrl,
            host = context.requestHost,
            pageHost = context.pageHost,
            resourceType = context.resourceType
        )
    }

    fun matchRequest(
        url: String,
        host: String? = null,
        pageHost: String? = null,
        resourceType: ResourceType = ResourceType.UNKNOWN
    ): RuleMatchResult {
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

    fun matchRequestCandidates(context: RequestContext): List<RuleMatchResult> {
        return matchRequestCandidates(
            url = context.requestUrl,
            host = context.requestHost,
            pageHost = context.pageHost,
            resourceType = context.resourceType
        )
    }

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

    fun rules(): List<Rule> {
        return compiledRules.requestRules()
    }

    fun elementRules(): List<ElementRule> {
        return compiledRules.elementRules()
    }

    fun skippedRules(): List<SkippedRule> {
        return compiledRules.skippedRules
    }

    fun cleanNavigationUrl(url: String, pageUrl: String? = null): String {
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

    fun cssSelectorsFor(pageUrl: String?): List<String> {
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

    fun domSelectorsFor(pageUrl: String?): List<String> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.domRemoveCandidatesFor(pageHost)
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .distinct()
    }

    fun scriptletWindowOpenBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletArgumentsFor(
            pageUrl = pageUrl,
            hookName = "window-open-block-keyword"
        )
    }

    fun scriptletFetchBlockedKeywordsFor(pageUrl: String?): List<String> {
        return scriptletArgumentsFor(
            pageUrl = pageUrl,
            hookName = "fetch-block-keyword"
        )
    }

    fun isScriptletSkipButtonsEnabledFor(pageUrl: String?): Boolean {
        return scriptletHooksFor(pageUrl).any { capability ->
            capability.hookName == "click-skip-buttons"
        }
    }

    fun isScriptletVideoControlsEnabledFor(pageUrl: String?): Boolean {
        return scriptletHooksFor(pageUrl).any { capability ->
            capability.hookName == "enable-video-controls"
        }
    }

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

    private fun decodedQueryName(rawName: String): String {
        return runCatching {
            URLDecoder.decode(rawName, StandardCharsets.UTF_8.name())
        }.getOrDefault(rawName)
    }

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

    private fun scriptletArgumentsFor(pageUrl: String?, hookName: String): List<String> {
        return scriptletHooksFor(pageUrl)
            .filter { capability -> capability.hookName == hookName }
            .flatMap { capability -> capability.arguments }
            .distinct()
    }

    private fun scriptletHooksFor(pageUrl: String?): List<RuleCapability.SafeHook> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.safeHookCapabilities.filter { capability ->
            capability.domainScope.matches(pageHost)
        }
    }
}
