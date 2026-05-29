package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

data class DomainScope(
    val includedDomains: Set<String> = emptySet(),
    val excludedDomains: Set<String> = emptySet()
) {
    val normalizedIncludedDomains: Set<String> = includedDomains.mapNotNull(SiteHost::normalize).toSet()
    val normalizedExcludedDomains: Set<String> = excludedDomains.mapNotNull(SiteHost::normalize).toSet()

    val hasRestrictions: Boolean
        get() = normalizedIncludedDomains.isNotEmpty() || normalizedExcludedDomains.isNotEmpty()

    fun matches(host: String?): Boolean {
        if (!hasRestrictions) {
            return true
        }

        val normalizedHost = SiteHost.normalize(host) ?: return false
        if (normalizedExcludedDomains.any { domain -> hostMatchesDomain(normalizedHost, domain) }) {
            return false
        }
        if (normalizedIncludedDomains.isEmpty()) {
            return true
        }
        return normalizedIncludedDomains.any { domain -> hostMatchesDomain(normalizedHost, domain) }
    }

    companion object {
        val Empty = DomainScope()

        fun hostMatchesDomain(host: String, domain: String): Boolean {
            return host == domain || host.endsWith(".$domain")
        }
    }
}
