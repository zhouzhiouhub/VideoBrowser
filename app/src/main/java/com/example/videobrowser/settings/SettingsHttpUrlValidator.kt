package com.example.videobrowser.settings

import java.net.URI

internal object SettingsHttpUrlValidator {
    fun isHttpUrl(url: String): Boolean {
        val uri = runCatching { URI(url) }.getOrNull() ?: return false
        val scheme = uri.scheme ?: return false
        return (scheme.equals("http", ignoreCase = true) ||
            scheme.equals("https", ignoreCase = true)) &&
            !uri.host.isNullOrBlank()
    }
}
