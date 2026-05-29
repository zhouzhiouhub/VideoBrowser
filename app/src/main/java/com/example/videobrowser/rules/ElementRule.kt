package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

data class ElementRule(
    val id: String,
    val selector: String,
    val type: ElementRuleType,
    val source: String = Rule.SOURCE_BUILT_IN,
    val domains: Set<String> = emptySet(),
    val excludedDomains: Set<String> = emptySet()
) {
    init {
        require(id.isNotBlank()) { "Element rule id must not be blank." }
        require(selector.trim().isNotEmpty()) { "Element selector must not be blank." }
    }

    val normalizedDomains: Set<String> = domains.mapNotNull(SiteHost::normalize).toSet()
    val normalizedExcludedDomains: Set<String> = excludedDomains.mapNotNull(SiteHost::normalize).toSet()

    fun matchesPage(pageUrl: String?): Boolean {
        return DomainScope(
            includedDomains = normalizedDomains,
            excludedDomains = normalizedExcludedDomains
        ).matches(SiteHost.fromUrl(pageUrl))
    }
}

enum class ElementRuleType {
    CSS_HIDE,
    CSS_UNHIDE,
    DOM_REMOVE
}
