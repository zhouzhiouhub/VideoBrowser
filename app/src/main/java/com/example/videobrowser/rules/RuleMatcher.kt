package com.example.videobrowser.rules

import java.net.URI
import java.util.Locale

class RuleMatcher {
    fun matches(
        rule: Rule,
        url: String,
        host: String? = null,
        pageHost: String? = null
    ): Boolean {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isEmpty()) {
            return false
        }
        if (!rule.domainScope.matches(pageHost)) {
            return false
        }
        if (!matchesPartyOption(rule, host ?: parseHost(url), pageHost)) {
            return false
        }

        return when (rule.type) {
            RuleType.URL_CONTAINS -> normalizedUrl.contains(rule.normalizedPattern)
            RuleType.URL_PATTERN -> rule.normalizedPatternRegex?.containsMatchIn(normalizedUrl) ?: false
            RuleType.DOMAIN_CONTAINS -> matchesDomain(
                host = normalizeHost(host ?: parseHost(url)),
                domain = normalizeHost(rule.pattern)
            )
        }
    }

    private fun matchesPartyOption(rule: Rule, requestHost: String?, pageHost: String?): Boolean {
        val expectedThirdParty = rule.thirdParty ?: return true
        val actualThirdParty = isThirdParty(requestHost, pageHost) ?: return false
        return actualThirdParty == expectedThirdParty
    }

    private fun isThirdParty(requestHost: String?, pageHost: String?): Boolean? {
        val requestSite = effectiveSite(normalizeHost(requestHost)).takeIf { it.isNotEmpty() }
            ?: return null
        val pageSite = effectiveSite(normalizeHost(pageHost)).takeIf { it.isNotEmpty() }
            ?: return null
        return requestSite != pageSite
    }

    private fun effectiveSite(host: String): String {
        val labels = host.split('.').filter { label -> label.isNotEmpty() }
        if (labels.size <= 2) {
            return host
        }
        val suffix = labels.takeLast(2)
        if (suffix[1].length == 2 && suffix[0] in COMMON_SECOND_LEVEL_SUFFIXES && labels.size >= 3) {
            return labels.takeLast(3).joinToString(".")
        }
        return suffix.joinToString(".")
    }

    private fun matchesDomain(host: String, domain: String): Boolean {
        if (host.isEmpty() || domain.isEmpty()) {
            return false
        }

        return host == domain || host.endsWith(".$domain")
    }

    private fun normalizeUrl(url: String): String {
        return url.trim().lowercase(Locale.US)
    }

    private fun normalizeHost(host: String?): String {
        return host.orEmpty()
            .trim()
            .trim('.')
            .lowercase(Locale.US)
    }

    private fun parseHost(url: String): String? {
        return runCatching { URI(url.trim()).host }.getOrNull()
    }

    private companion object {
        private val COMMON_SECOND_LEVEL_SUFFIXES = setOf(
            "ac",
            "co",
            "com",
            "edu",
            "gov",
            "net",
            "org"
        )
    }
}
