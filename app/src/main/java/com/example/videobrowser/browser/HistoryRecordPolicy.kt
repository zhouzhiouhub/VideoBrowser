package com.example.videobrowser.browser

import java.net.URI
import java.util.Locale

class HistoryRecordPolicy(
    private val homeUrls: () -> List<String>
) {
    fun shouldRecord(url: String?): Boolean {
        val currentUrl = WebUrl.from(url) ?: return false
        return homeUrls()
            .mapNotNull(WebUrl::from)
            .none { homeUrl -> homeUrl.isSamePageAs(currentUrl) }
    }

    private data class WebUrl(
        val scheme: String,
        val host: String,
        val port: Int,
        val path: String
    ) {
        fun isSamePageAs(other: WebUrl): Boolean {
            return scheme == other.scheme &&
                host == other.host &&
                port == other.port &&
                path == other.path
        }

        companion object {
            fun from(value: String?): WebUrl? {
                val uri = try {
                    URI(value?.trim().orEmpty())
                } catch (_: IllegalArgumentException) {
                    return null
                }
                val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
                if (scheme != "http" && scheme != "https") {
                    return null
                }
                val host = uri.host
                    ?.trimEnd('.')
                    ?.lowercase(Locale.US)
                    ?.takeIf { it.isNotBlank() }
                    ?: return null
                return WebUrl(
                    scheme = scheme,
                    host = host,
                    port = normalizedPort(scheme, uri.port),
                    path = uri.rawPath.orEmpty().trim('/')
                )
            }

            private fun normalizedPort(scheme: String, port: Int): Int {
                if (port >= 0) {
                    return port
                }
                return when (scheme) {
                    "http" -> 80
                    "https" -> 443
                    else -> -1
                }
            }
        }
    }
}
