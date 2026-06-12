package com.example.videobrowser.browser

import java.util.Locale

enum class SiteSecurityStatus {
    SECURE,
    NOT_SECURE,
    UNKNOWN;

    fun protocolDisplayName(): String {
        return when (this) {
            SECURE -> "HTTPS"
            NOT_SECURE -> "HTTP"
            UNKNOWN -> "未知"
        }
    }

    fun isEncryptedConnection(): Boolean {
        return this == SECURE
    }

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
