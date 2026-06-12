package com.example.videobrowser.download

import java.net.URI
import java.util.Locale

object DownloadSafetyPolicy {
    fun requiresConfirmation(fileName: String, mimeType: String?): Boolean {
        return DownloadCategory.from(mimeType, fileName) == DownloadCategory.APP
    }

    fun requiresInsecureTransportConfirmation(pageUrl: String?, downloadUrl: String): Boolean {
        return schemeOf(pageUrl) == "https" && schemeOf(downloadUrl) == "http"
    }

    fun isDownloadableNetworkUrl(url: String): Boolean {
        val uri = uriOf(url) ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        return (scheme == "http" || scheme == "https") &&
            !uri.host.isNullOrBlank()
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
