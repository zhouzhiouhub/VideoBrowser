package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.UrlUtils

/**
 * Owns recognition of search result pages created by the app's configured
 * search providers.
 */
class BuiltInSearchResultPagePolicy(
    private val providers: () -> Collection<SearchProvider>
) {
    constructor(providers: Collection<SearchProvider>) : this({ providers })

    fun isBuiltInSearchResultUrl(url: String?): Boolean {
        return searchQueryFromUrl(url) != null
    }

    fun searchQueryFromUrl(url: String?): String? {
        val normalizedUrl = url?.trim().orEmpty()
        if (normalizedUrl.isBlank()) {
            return null
        }
        providers().forEach { provider ->
            provider.addressBarSearchUrlPrefixes.forEach { searchUrlPrefix ->
                UrlUtils.searchQueryFromUrl(normalizedUrl, searchUrlPrefix)?.let { return it }
            }
        }
        return null
    }
}
