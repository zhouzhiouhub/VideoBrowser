package com.example.videobrowser.browser.search

import com.example.videobrowser.browser.BrowserRequest

/**
 * Keeps search result pages responsive by skipping heavy interception for
 * first-party search provider resources. Cross-site resources still use the
 * normal ad-block and smart-no-image chain.
 */
class SearchResultRequestInterceptionPolicy(
    private val isSearchResultResourceUrl: (pageUrl: String?, resourceUrl: String?) -> Boolean =
        { _, _ -> false }
) {
    fun shouldBypassHeavyInterception(request: BrowserRequest): Boolean {
        if (request.isForMainFrame) {
            return false
        }
        return isSearchResultResourceUrl(request.pageUrl, request.url.toString())
    }
}
