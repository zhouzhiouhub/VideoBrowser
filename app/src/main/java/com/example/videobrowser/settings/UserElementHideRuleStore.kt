package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class UserElementHideRuleStore(
    private val preferenceStore: PreferenceStore
) {
    private val lineStore = PreferenceLineStore(preferenceStore, KEY_USER_ELEMENT_HIDE_RULES)

    fun selectorsForSite(host: String?): List<String> {
        val normalizedHost = SiteHost.normalize(host) ?: return emptyList()
        return load()
            .filter { rule -> rule.host == normalizedHost }
            .map { rule -> rule.selector }
    }

    fun hasSelectorForSite(host: String?, selector: String): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val normalizedSelector = normalizeSelector(selector) ?: return false
        return load().any { rule ->
            rule.host == normalizedHost && rule.selector == normalizedSelector
        }
    }

    fun addSelectorForSite(host: String?, selector: String): Boolean {
        val normalizedHost = SiteHost.normalize(host) ?: return false
        val normalizedSelector = normalizeSelector(selector) ?: return false
        val rules = load().toMutableList()
        if (rules.any { rule -> rule.host == normalizedHost && rule.selector == normalizedSelector }) {
            return false
        }

        rules.add(UserElementHideRule(host = normalizedHost, selector = normalizedSelector))
        save(rules)
        return true
    }

    fun load(): List<UserElementHideRule> {
        return lineStore.loadLines()
            .mapNotNull(::parseLine)
            .distinct()
            .toList()
    }

    fun remove(rule: UserElementHideRule): Boolean {
        val normalizedHost = SiteHost.normalize(rule.host) ?: return false
        val normalizedSelector = normalizeSelector(rule.selector) ?: return false
        val rules = load()
        val remainingRules = rules.filterNot { existingRule ->
            existingRule.host == normalizedHost && existingRule.selector == normalizedSelector
        }
        if (remainingRules.size == rules.size) {
            return false
        }

        save(remainingRules)
        return true
    }

    fun clear() {
        lineStore.clear()
    }

    private fun parseLine(line: String): UserElementHideRule? {
        val fields = TabSeparatedLineCodec.splitPair(line) ?: return null
        val host = SiteHost.normalize(fields.first) ?: return null
        val selector = normalizeSelector(fields.second) ?: return null
        return UserElementHideRule(host = host, selector = selector)
    }

    private fun save(rules: List<UserElementHideRule>) {
        val lines = rules
            .mapNotNull { rule ->
                val host = SiteHost.normalize(rule.host) ?: return@mapNotNull null
                val selector = normalizeSelector(rule.selector) ?: return@mapNotNull null
                UserElementHideRule(host = host, selector = selector)
            }
            .distinct()
            .sortedWith(compareBy<UserElementHideRule> { it.host }.thenBy { it.selector })
            .map { rule -> TabSeparatedLineCodec.joinPair(rule.host, rule.selector) }

        lineStore.saveLines(lines)
    }

    private fun normalizeSelector(selector: String): String? {
        return SettingsCssSelectorNormalizer.normalize(selector)
    }
}
