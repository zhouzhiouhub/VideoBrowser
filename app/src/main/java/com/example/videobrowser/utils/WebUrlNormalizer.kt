package com.example.videobrowser.utils

object WebUrlNormalizer {
    fun normalizeHttpOrHttpsUrl(url: String?): String? {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uri = SafeUriParser.parse(normalizedUrl) ?: return null
        if (!WebSchemePolicy.isHttpOrHttpsScheme(uri.scheme)) {
            return null
        }
        if (uri.host.isNullOrBlank()) {
            return null
        }
        return normalizedUrl
    }
}
