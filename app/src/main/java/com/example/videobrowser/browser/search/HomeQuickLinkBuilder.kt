package com.example.videobrowser.browser.search

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
