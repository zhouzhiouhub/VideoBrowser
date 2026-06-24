package com.example.videobrowser.browser.search

import com.example.videobrowser.browser.BrowserHomePageUrlPolicy

internal object SearchProviderHomeMatcher {
    fun isProviderHomeUrl(url: String?, providers: Collection<SearchProvider>): Boolean {
        return BrowserHomePageUrlPolicy(
            homeUrls = { providers.map { provider -> provider.homeUrl } }
        ).isHomeUrl(url)
    }
}
