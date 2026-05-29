package com.example.videobrowser.rules

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.site.SiteHost

class RuleEngine(
    rules: List<Rule>,
    elementRules: List<ElementRule> = emptyList(),
    private val ruleMatcher: RuleMatcher = RuleMatcher(),
    ruleCompiler: RuleCompiler = RuleCompiler()
) {
    // G2-01 起 RuleEngine 只消费编译后的能力模型，索引层后续可在这里替换候选规则来源。
    private val compiledRules = ruleCompiler.compile(
        requestRules = rules,
        elementRules = elementRules
    )
    private val requestCapabilities = compiledRules.requestCapabilities
    private val cssHideCapabilities = compiledRules.cssHideCapabilities
    private val cssUnhideCapabilities = compiledRules.cssUnhideCapabilities
    private val domRemoveCapabilities = compiledRules.domRemoveCapabilities

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

    fun rules(): List<Rule> {
        return compiledRules.requestRules()
    }

    fun elementRules(): List<ElementRule> {
        return compiledRules.elementRules()
    }

    fun skippedRules(): List<SkippedRule> {
        return compiledRules.skippedRules
    }

    fun cssSelectorsFor(pageUrl: String?): List<String> {
        val exceptions = cssUnhideCapabilities
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .toSet()
        return cssHideCapabilities
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .filterNot { selector -> selector in exceptions }
            .distinct()
    }

    fun domSelectorsFor(pageUrl: String?): List<String> {
        return domRemoveCapabilities
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .distinct()
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

    private fun findFirstMatchingRule(
        action: RuleAction,
        url: String,
        host: String?,
        pageHost: String?,
        resourceType: ResourceType
    ): Rule? {
        return requestCapabilities.firstOrNull { capability ->
            val rule = capability.rule
            rule.action == action && ruleMatcher.matches(
                rule = rule,
                url = url,
                host = host,
                pageHost = pageHost,
                resourceType = resourceType
            )
        }?.rule
    }
}
