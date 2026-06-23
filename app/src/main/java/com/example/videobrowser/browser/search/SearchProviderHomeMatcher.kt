package com.example.videobrowser.browser.search

import com.example.videobrowser.utils.WebPageIdentity

internal object SearchProviderHomeMatcher {
    fun isProviderHomeUrl(url: String?, providers: Collection<SearchProvider>): Boolean {
        val currentPage = WebPageIdentity.from(url) ?: return false
        return providers.any { provider ->
            WebPageIdentity.from(provider.homeUrl)
                ?.isSamePageAs(currentPage)
                ?: false
        }
    }
}
