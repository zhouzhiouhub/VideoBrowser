package com.example.videobrowser.browser.search

/**
 * 初学者阅读提示：
 * 这个文件属于“搜索入口与地址建议模块”。
 * 文件名 HomeQuickLinkBuilder 可以拆开理解为“Home Quick Link Builder”，表示它只负责浏览器流程中的一个小职责。
 * 主要职责：把地址栏输入、默认搜索引擎、远程搜索建议、收藏和历史候选项整理成用户可以点击的建议列表。
 * 阅读顺序：先看构造参数知道它依赖谁，再看公开函数知道外部如何调用，最后看 private 函数了解内部细节。
 */
import com.example.videobrowser.storage.SavedPage
import java.net.URI
import java.util.Locale

data class HomeQuickLink(
    val title: String,
    val url: String
)

object HomeQuickLinkBuilder {
    fun fromHistory(
        history: List<SavedPage>,
        excludedUrls: Collection<String>,
        limit: Int = DEFAULT_LIMIT
    ): List<HomeQuickLink> {
        if (limit <= 0) {
            return emptyList()
        }
        val excludedKeys = excludedUrls.mapNotNull(::urlKey).toSet()
        val seenKeys = mutableSetOf<String>()
        return history.asSequence()
            .mapNotNull { page ->
                val key = urlKey(page.url) ?: return@mapNotNull null
                if (key in excludedKeys || !seenKeys.add(key)) {
                    return@mapNotNull null
                }
                HomeQuickLink(
                    title = page.title.trim().ifBlank { displayHost(page.url) ?: page.url.trim() },
                    url = page.url.trim()
                )
            }
            .take(limit)
            .toList()
    }

    private fun urlKey(url: String): String? {
        val uri = runCatching { URI(url.trim()) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return null
        val host = uri.host?.lowercase(Locale.ROOT) ?: return null
        if (scheme != "http" && scheme != "https") {
            return null
        }
        return uri.toString().lowercase(Locale.ROOT).takeIf { host.isNotBlank() }
    }

    private fun displayHost(url: String): String? {
        return runCatching { URI(url.trim()).host }
            .getOrNull()
            ?.takeIf { host -> host.isNotBlank() }
    }

    private const val DEFAULT_LIMIT = 4
}
