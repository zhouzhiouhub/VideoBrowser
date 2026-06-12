package com.example.videobrowser.browser

import java.util.Locale

enum class SiteSecurityStatus {
    SECURE,
    NOT_SECURE,
    UNKNOWN;

    companion object {
        fun fromUrl(url: String?): SiteSecurityStatus {
            val scheme = url
                ?.substringBefore(':', missingDelimiterValue = "")
                ?.trim()
                ?.lowercase(Locale.ROOT)
                .orEmpty()
            return when (scheme) {
                "https" -> SECURE
                "http" -> NOT_SECURE
                else -> UNKNOWN
            }
        }
    }
}
