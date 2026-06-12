package com.example.videobrowser.functioncenter

import com.example.videobrowser.browser.BrowserTab
import com.example.videobrowser.utils.UrlUtils
import java.net.URI

object BrowserTabDisplayText {
    fun title(tab: BrowserTab, untitledText: String): String {
        return tab.title
            .takeIf { it.isNotBlank() }
            ?: tab.url?.let(::compactUrlTitle)?.takeIf { it.isNotBlank() }
            ?: untitledText
    }

    private fun compactUrlTitle(url: String): String {
        val parsed = runCatching { URI(url.trim()) }.getOrNull()
        return parsed?.rawAuthority
            ?.takeIf { it.isNotBlank() }
            ?: UrlUtils.displayUrl(url)
    }
}
