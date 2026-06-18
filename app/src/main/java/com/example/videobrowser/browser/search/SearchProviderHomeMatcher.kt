package com.example.videobrowser.browser.search

import android.net.Uri

internal object SearchProviderHomeMatcher {
    fun isProviderHomeUrl(url: String?, providers: Collection<SearchProvider>): Boolean {
        if (url.isNullOrBlank()) {
            return false
        }

        val currentUri = Uri.parse(url)
        return providers.any { provider ->
            val homeUri = Uri.parse(provider.homeUrl)
            currentUri.scheme.equals(homeUri.scheme, ignoreCase = true) &&
                currentUri.host.equals(homeUri.host, ignoreCase = true) &&
                normalizedPath(currentUri) == normalizedPath(homeUri)
        }
    }

    private fun normalizedPath(uri: Uri): String {
        return uri.path.orEmpty().trim('/')
    }
}
