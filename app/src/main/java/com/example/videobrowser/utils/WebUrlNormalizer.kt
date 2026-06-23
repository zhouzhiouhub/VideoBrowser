package com.example.videobrowser.utils

object WebUrlNormalizer {
    fun normalizeHttpOrHttpsUrl(url: String?): String? {
        return normalizeNetworkUrl(url) { scheme ->
            WebSchemePolicy.isHttpOrHttpsScheme(scheme)
        }
    }

    fun normalizeHttpUrl(url: String?): String? {
        return normalizeNetworkUrl(url) { scheme ->
            WebSchemePolicy.isHttpScheme(scheme)
        }
    }

    fun isHttpOrHttpsUrl(url: String?): Boolean {
        return normalizeHttpOrHttpsUrl(url) != null
    }

    fun isHttpUrl(url: String?): Boolean {
        return normalizeHttpUrl(url) != null
    }

    private fun normalizeNetworkUrl(url: String?, isAllowedScheme: (String?) -> Boolean): String? {
        val normalizedUrl = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val uri = SafeUriParser.parse(normalizedUrl) ?: return null
        if (!isAllowedScheme(uri.scheme)) {
            return null
        }
        if (uri.host.isNullOrBlank()) {
            return null
        }
        return normalizedUrl
    }
}
