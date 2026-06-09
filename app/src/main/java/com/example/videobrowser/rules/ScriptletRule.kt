package com.example.videobrowser.rules

import com.example.videobrowser.site.SiteHost

data class ScriptletRule(
    val id: String,
    val name: String,
    val arguments: List<String> = emptyList(),
    val source: String = Rule.SOURCE_BUILT_IN,
    val domainScope: DomainScope = DomainScope.Empty
) {
    init {
        require(id.isNotBlank()) { "Scriptlet rule id must not be blank." }
        require(name.isNotBlank()) { "Scriptlet name must not be blank." }
    }

    fun matchesPage(pageUrl: String?): Boolean {
        return domainScope.matches(SiteHost.fromUrl(pageUrl))
    }
}
