package com.example.videobrowser.utils

import java.util.Locale

data class WebPageIdentity(
    val scheme: String,
    val host: String,
    val port: Int,
    val path: String
) {
    fun isSamePageAs(other: WebPageIdentity): Boolean {
        return scheme == other.scheme &&
            host == other.host &&
            port == other.port &&
            path == other.path
    }

    companion object {
        fun from(value: String?): WebPageIdentity? {
            val uri = SafeUriParser.parse(value) ?: return null
            val scheme = uri.scheme?.lowercase(Locale.US) ?: return null
            if (!WebSchemePolicy.isHttpOrHttpsScheme(scheme)) {
                return null
            }
            val host = HostNameNormalizer.normalize(uri.host) ?: return null
            return WebPageIdentity(
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
