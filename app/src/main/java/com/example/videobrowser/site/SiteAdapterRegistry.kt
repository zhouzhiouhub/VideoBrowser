package com.example.videobrowser.site

/**
 * 初学者阅读提示：
 * 这个文件属于“站点适配模块”。
 * 文件名 SiteAdapterRegistry 可以拆开理解为“Site Adapter Registry”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：识别不同视频网站或网页宿主，并把站点专属能力交给通用浏览器流程使用。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import java.net.URI
import java.util.Locale

/**
 * 站点适配器注册表。
 *
 * 通用脚本先处理大多数网页；当 URL 命中特定站点时，这里会返回额外的站点脚本。
 * 例如某些视频网站有自定义播放器 API，需要专门脚本把播放、倍速、进度控制接到统一协议。
 */
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
