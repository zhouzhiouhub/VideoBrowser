package com.example.videobrowser.site

import java.net.URI
import java.util.Locale

class SiteAdapterRegistry(
    private val adapters: List<SiteAdapter>
) {
    fun matchingAdapters(url: String?): List<SiteAdapter> {
        val normalizedUrl = url?.trim().orEmpty()
        if (normalizedUrl.isBlank()) {
            return emptyList()
        }
        return adapters.filter { adapter -> adapter.matches(normalizedUrl) }
    }

    fun scriptFilesFor(url: String?): List<String> {
        return matchingAdapters(url)
            .flatMap { adapter -> adapter.scriptFiles() }
            .distinct()
    }

    companion object {
        fun default(): SiteAdapterRegistry {
            return SiteAdapterRegistry(
                listOf(
                    domainAdapter(
                        id = "youtube",
                        displayName = "YouTube",
                        domains = setOf("youtube.com"),
                        scriptAssetPath = "scripts/youtube.js"
                    ),
                    domainAdapter(
                        id = "bilibili",
                        displayName = "Bilibili",
                        domains = setOf("bilibili.com"),
                        scriptAssetPath = "scripts/bilibili.js"
                    ),
                    domainAdapter(
                        id = "iqiyi",
                        displayName = "iQIYI",
                        domains = setOf("iqiyi.com"),
                        scriptAssetPath = "scripts/iqiyi.js"
                    ),
                    domainAdapter(
                        id = "tencent",
                        displayName = "Tencent Video",
                        domains = setOf("v.qq.com"),
                        scriptAssetPath = "scripts/tencent.js"
                    ),
                    domainAdapter(
                        id = "youku",
                        displayName = "Youku",
                        domains = setOf("youku.com"),
                        scriptAssetPath = "scripts/youku.js"
                    )
                )
            )
        }

        private fun domainAdapter(
            id: String,
            displayName: String,
            domains: Set<String>,
            scriptAssetPath: String
        ): SiteAdapter {
            return DomainSiteAdapter(
                SiteProfile(
                    id = id,
                    displayName = displayName,
                    domains = domains,
                    scriptAssetPaths = listOf(scriptAssetPath)
                )
            )
        }
    }
}

private class DomainSiteAdapter(
    override val profile: SiteProfile
) : SiteAdapter {
    private val domains = profile.domains.mapNotNull(::normalizeHost)

    override fun matches(url: String): Boolean {
        val host = hostFromUrl(url) ?: return false
        return domains.any { domain ->
            host == domain || host.endsWith(".$domain")
        }
    }
}

private fun hostFromUrl(url: String): String? {
    val value = url.trim()
    if (value.isBlank()) {
        return null
    }

    val urlWithScheme = if (value.contains("://")) {
        value
    } else {
        "https://$value"
    }
    return runCatching {
        URI(urlWithScheme).host
    }.getOrNull()?.let(::normalizeHost)
}

private fun normalizeHost(host: String): String? {
    val normalized = host
        .trim()
        .trimEnd('.')
        .lowercase(Locale.US)
    return normalized.takeIf { it.isNotBlank() }
}
