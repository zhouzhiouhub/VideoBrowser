package com.example.videobrowser.browser

import java.net.URLDecoder
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Locale

object ExternalProtocolPolicy {
    private val blockedSchemes = setOf(
        "about",
        "blob",
        "chrome",
        "content",
        "data",
        "file",
        "http",
        "https",
        "javascript",
        "view-source"
    )

    fun shouldOpenExternally(scheme: String?): Boolean {
        val normalizedScheme = scheme?.trim()?.lowercase(Locale.ROOT) ?: return false
        if (normalizedScheme.isEmpty()) {
            return false
        }
        return normalizedScheme !in blockedSchemes
    }

    fun isWebUrl(url: String?): Boolean {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return false
        val uri = runCatching { URI(normalizedUrl) }.getOrNull() ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false
        if (scheme != "http" && scheme != "https") {
            return false
        }
        return !uri.host.isNullOrBlank()
    }

    fun fallbackUrlFromIntentUri(intentUri: String): String? {
        if (!intentUri.startsWith("intent:", ignoreCase = true)) {
            return null
        }
        val marker = "S.$BROWSER_FALLBACK_URL="
        val start = intentUri.indexOf(marker)
        if (start < 0) {
            return null
        }
        val valueStart = start + marker.length
        val valueEnd = intentUri.indexOf(';', valueStart).takeIf { it >= 0 } ?: intentUri.length
        val rawValue = intentUri.substring(valueStart, valueEnd)
        val decoded = runCatching {
            URLDecoder.decode(
                rawValue.replace("+", "%2B"),
                StandardCharsets.UTF_8.name()
            )
        }.getOrNull()
        return decoded?.takeIf(::isWebUrl)
    }

    const val BROWSER_FALLBACK_URL = "browser_fallback_url"
}
