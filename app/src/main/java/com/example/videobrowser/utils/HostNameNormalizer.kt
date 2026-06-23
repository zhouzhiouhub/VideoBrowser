package com.example.videobrowser.utils

import java.util.Locale

object HostNameNormalizer {
    fun normalize(host: String?): String? {
        val normalized = host
            ?.trim()
            ?.trim('.')
            ?.lowercase(Locale.ROOT)
            .orEmpty()
        return normalized.takeIf { it.isNotEmpty() }
    }

    fun fromUrl(url: String?): String? {
        val value = url?.trim().orEmpty()
        if (value.isEmpty()) {
            return null
        }
        return normalize(SafeUriParser.parse(value)?.host)
    }

    fun fromUrlOrBareHost(url: String?): String? {
        val value = url?.trim().orEmpty()
        if (value.isBlank()) {
            return null
        }
        val urlWithScheme = if (value.contains("://")) {
            value
        } else {
            "https://$value"
        }
        return normalize(SafeUriParser.parse(urlWithScheme)?.host)
    }

    fun matchesDomainOrSubdomain(host: String?, domain: String?): Boolean {
        val normalizedHost = normalize(host) ?: return false
        val normalizedDomain = normalize(domain) ?: return false
        return normalizedHost == normalizedDomain || normalizedHost.endsWith(".$normalizedDomain")
    }
}
