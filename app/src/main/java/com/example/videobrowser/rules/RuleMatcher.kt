package com.example.videobrowser.rules

import java.net.URI
import java.util.Locale

class RuleMatcher {
    fun matches(rule: Rule, url: String, host: String? = null): Boolean {
        val normalizedUrl = normalizeUrl(url)
        if (normalizedUrl.isEmpty()) {
            return false
        }

        return when (rule.type) {
            RuleType.URL_CONTAINS -> normalizedUrl.contains(rule.normalizedPattern)
            RuleType.DOMAIN_CONTAINS -> matchesDomain(
                host = normalizeHost(host ?: parseHost(url)),
                domain = normalizeHost(rule.pattern)
            )
        }
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
}
