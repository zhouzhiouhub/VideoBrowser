package com.example.videobrowser.rules

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.browser.RequestContext
import com.example.videobrowser.browser.ResourceType
import com.example.videobrowser.site.SiteHost

class RuleEngine(
    rules: List<Rule>,
    elementRules: List<ElementRule> = emptyList(),
    private val ruleMatcher: RuleMatcher = RuleMatcher()
) {
    private val requestRules = rules.toList()
    private val elementRules = elementRules.toList()

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
        return requestRules
    }

    fun elementRules(): List<ElementRule> {
        return elementRules
    }

    fun cssSelectorsFor(pageUrl: String?): List<String> {
        val exceptions = elementRules
            .filter { rule ->
                rule.type == ElementRuleType.CSS_UNHIDE && rule.matchesPage(pageUrl)
            }
            .map { rule -> rule.selector }
            .toSet()
        return elementRules
            .filter { rule ->
                rule.type == ElementRuleType.CSS_HIDE && rule.matchesPage(pageUrl)
            }
            .map { rule -> rule.selector }
            .filterNot { selector -> selector in exceptions }
            .distinct()
    }

    fun domSelectorsFor(pageUrl: String?): List<String> {
        return elementRules
            .filter { rule ->
                rule.type == ElementRuleType.DOM_REMOVE && rule.matchesPage(pageUrl)
            }
            .map { rule -> rule.selector }
            .distinct()
    }

    fun urlContainsBlockPatternsFor(pageUrl: String?): List<String> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return requestRules
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
        return requestRules.firstOrNull { rule ->
            rule.action == action && ruleMatcher.matches(
                rule = rule,
                url = url,
                host = host,
                pageHost = pageHost,
                resourceType = resourceType
            )
        }
    }
}
