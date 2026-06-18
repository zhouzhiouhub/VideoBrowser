package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

internal class RuleElementSelectorQuery(
    private val compiledRules: CompiledRuleSet
) {
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

    fun domSelectorsFor(pageUrl: String?): List<String> {
        val pageHost = SiteHost.fromUrl(pageUrl)
        return compiledRules.domRemoveCandidatesFor(pageHost)
            .filter { rule ->
                rule.rule.matchesPage(pageUrl)
            }
            .map { capability -> capability.rule.selector }
            .distinct()
    }
}
