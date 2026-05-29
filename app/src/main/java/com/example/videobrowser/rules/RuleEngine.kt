package com.example.videobrowser.rules

import com.example.videobrowser.browser.BrowserRequest

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

    fun matchRequest(url: String, host: String? = null): RuleMatchResult {
        findFirstMatchingRule(
            action = RuleAction.ALLOW,
            url = url,
            host = host
        )?.let { allowRule ->
            return RuleMatchResult.allow(allowRule)
        }

        findFirstMatchingRule(
            action = RuleAction.BLOCK,
            url = url,
            host = host
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
        return elementRules
            .filter { rule ->
                rule.type == ElementRuleType.CSS_HIDE && rule.matchesPage(pageUrl)
            }
            .map { rule -> rule.selector }
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

    private fun findFirstMatchingRule(
        action: RuleAction,
        url: String,
        host: String?
    ): Rule? {
        return requestRules.firstOrNull { rule ->
            rule.action == action && ruleMatcher.matches(
                rule = rule,
                url = url,
                host = host
            )
        }
    }
}
