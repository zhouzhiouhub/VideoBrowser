package com.example.videobrowser.rules

import com.example.videobrowser.browser.BrowserRequest
import com.example.videobrowser.site.SiteHost

class RuleEngine(
    rules: List<Rule>,
    elementRules: List<ElementRule> = emptyList(),
    private val ruleMatcher: RuleMatcher = RuleMatcher()
) {
    private val requestRules = rules.toList()
    private val elementRules = elementRules.toList()

    fun matchRequest(request: BrowserRequest): RuleMatchResult {
        return matchRequest(
            url = request.url.toString(),
            host = request.url.host
        )
    }

    fun matchRequest(
        url: String,
        host: String? = null,
        pageHost: String? = null
    ): RuleMatchResult {
        findFirstMatchingRule(
            action = RuleAction.ALLOW,
            url = url,
            host = host,
            pageHost = pageHost
        )?.let { allowRule ->
            return RuleMatchResult.allow(allowRule)
        }

        findFirstMatchingRule(
            action = RuleAction.BLOCK,
            url = url,
            host = host,
            pageHost = pageHost
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
                    rule.domainScope.matches(pageHost)
            }
            .map { rule -> rule.pattern }
            .distinct()
    }

    private fun findFirstMatchingRule(
        action: RuleAction,
        url: String,
        host: String?,
        pageHost: String?
    ): Rule? {
        return requestRules.firstOrNull { rule ->
            rule.action == action && ruleMatcher.matches(
                rule = rule,
                url = url,
                host = host,
                pageHost = pageHost
            )
        }
    }
}
