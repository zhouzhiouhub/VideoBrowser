package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

internal object DomainScopeParser {
    fun parseCommaSeparated(value: String, requireDomain: Boolean = true): DomainScope? {
        return parse(value, separator = ",", requireDomain = requireDomain)
    }

    fun parsePipeSeparated(value: String): DomainScope? {
        return parse(value, separator = "|", requireDomain = false)
    }

    private fun parse(value: String, separator: String, requireDomain: Boolean): DomainScope? {
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return DomainScope.Empty
        }

        val included = mutableSetOf<String>()
        val excluded = mutableSetOf<String>()
        trimmed.split(separator)
            .map { domain -> domain.trim() }
            .filter { domain -> domain.isNotEmpty() }
            .forEach { rawDomain ->
                val isExcluded = rawDomain.startsWith("~")
                val normalized = SiteHost.normalize(rawDomain.removePrefix("~")) ?: return null
                if (!isValidDomainPattern(normalized)) {
                    return null
                }
                if (isExcluded) {
                    excluded += normalized
                } else {
                    included += normalized
                }
            }

        if (requireDomain && included.isEmpty() && excluded.isEmpty()) {
            return null
        }
        return DomainScope(
            includedDomains = included,
            excludedDomains = excluded
        )
    }

    private fun isValidDomainPattern(domain: String): Boolean {
        return domain.isNotBlank() &&
            domain.all { char -> char.isLetterOrDigit() || char == '-' || char == '.' }
    }
}
