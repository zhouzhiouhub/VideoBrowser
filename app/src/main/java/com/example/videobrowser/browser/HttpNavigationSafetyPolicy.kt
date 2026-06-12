package com.example.videobrowser.browser

import java.net.URI
import java.util.Locale

object HttpNavigationSafetyPolicy {
    fun requiresInsecureNavigationConfirmation(pageUrl: String?, targetUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && isHttpNetworkUrl(targetUrl)
    }

    private fun isHttpNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        return uri.scheme?.lowercase(Locale.ROOT) == "http" && !uri.host.isNullOrBlank()
    }

    private fun schemeOf(url: String?): String? {
        return uriOf(url)
            ?.scheme
            ?.lowercase(Locale.ROOT)
    }

    private fun uriOf(url: String?): URI? {
        return runCatching { URI(url?.trim().orEmpty()) }.getOrNull()
    }
}
