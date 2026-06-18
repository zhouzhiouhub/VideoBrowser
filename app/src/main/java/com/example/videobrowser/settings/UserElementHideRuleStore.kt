package com.example.videobrowser.settings

import com.example.videobrowser.site.SiteHost
import com.example.videobrowser.storage.PreferenceStore

internal class UserElementHideRuleStore(
    private val preferenceStore: PreferenceStore
) {
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
        return preferenceStore.getString(KEY_USER_ELEMENT_HIDE_RULES, null)
            ?.lineSequence()
            ?.mapNotNull(::parseLine)
            ?.distinct()
            ?.toList()
            ?: emptyList()
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
        preferenceStore.remove(KEY_USER_ELEMENT_HIDE_RULES)
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

        if (lines.isEmpty()) {
            preferenceStore.remove(KEY_USER_ELEMENT_HIDE_RULES)
        } else {
            preferenceStore.putString(KEY_USER_ELEMENT_HIDE_RULES, lines.joinToString(separator = "\n"))
        }
    }

    private fun normalizeSelector(selector: String): String? {
        val collapsed = selector.trim().replace(WHITESPACE_SEQUENCE, " ")
        val normalized = stabilizeSelector(collapsed)
        if (normalized.isEmpty() || normalized.length > MAX_USER_ELEMENT_SELECTOR_LENGTH) {
            return null
        }
        if (normalized.any { char -> char == '\t' || char == '\n' || char == '\r' }) {
            return null
        }
        if (UNSAFE_SELECTOR_PATTERN.containsMatchIn(normalized)) {
            return null
        }
        if (UNSUPPORTED_SELECTOR_PATTERN.containsMatchIn(normalized)) {
            return null
        }
        return normalized
    }

    private fun stabilizeSelector(selector: String): String {
        if (!selector.contains(":nth-of-type", ignoreCase = true) ||
            !STABLE_TOKEN_PATTERN.containsMatchIn(selector)
        ) {
            return selector
        }
        return POSITIONAL_SEGMENT_PATTERN
            .replace(selector, "")
            .replace(WHITESPACE_SEQUENCE, " ")
            .trim()
    }

    private companion object {
        private val WHITESPACE_SEQUENCE = Regex("\\s+")
        private val UNSAFE_SELECTOR_PATTERN = Regex("[{};<>]")
        private val UNSUPPORTED_SELECTOR_PATTERN =
            Regex(":has\\(|:contains\\(|:matches\\(|:xpath\\(|javascript:|expression\\(", RegexOption.IGNORE_CASE)
        private val POSITIONAL_SEGMENT_PATTERN =
            Regex(":nth-of-type\\(\\s*\\d+\\s*\\)", RegexOption.IGNORE_CASE)
        private val STABLE_TOKEN_PATTERN = Regex("[#.][A-Za-z_][A-Za-z0-9_-]*")
    }
}
